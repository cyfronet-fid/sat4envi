package pl.cyfronet.s4e.admin.institution;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.slugify.Slugify;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import pl.cyfronet.s4e.BasicTest;
import pl.cyfronet.s4e.Constants;
import pl.cyfronet.s4e.TestDbHelper;
import pl.cyfronet.s4e.bean.AppUser;
import pl.cyfronet.s4e.bean.Institution;
import pl.cyfronet.s4e.data.repository.AppUserRepository;
import pl.cyfronet.s4e.data.repository.InstitutionRepository;

import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static pl.cyfronet.s4e.TestJwtUtil.jwtBearerToken;

@BasicTest
@AutoConfigureMockMvc
class AdminInstitutionControllerTest {
    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private InstitutionRepository institutionRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private Slugify slugify;

    @Autowired
    private TestDbHelper testDbHelper;

    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    public void beforeEach() {
        testDbHelper.clean();
    }

    @Nested
    class Zk {
        private AppUser admin;

        private AppUser user;

        private Institution institution;

        @BeforeEach
        public void beforeEach() {
            admin = appUserRepository.save(AppUser.builder()
                    .email("admin@mail.pl")
                    .name("John")
                    .surname("Smith")
                    .password("{noop}password")
                    .enabled(true)
                    .admin(true)
                    .build());

            user = appUserRepository.save(AppUser.builder()
                    .email("user@mail.pl")
                    .name("John")
                    .surname("Smith")
                    .password("{noop}password")
                    .enabled(true)
                    .build());

            institution = institutionRepository.save(Institution.builder()
                    .name("A")
                    .slug(slugify.slugify("A"))
                    .build());
        }

        private void createHierarchy(boolean zk, String... relations) {
            assertThat(relations.length % 2, is(equalTo(0)));

            for (int i = 0; i < relations.length; i = i + 2) {
                String parentName = relations[i];
                String childName = relations[i + 1];

                val parent = institutionRepository.findBySlug(slugify.slugify(parentName)).get();
                institutionRepository.save(Institution.builder()
                        .name(childName)
                        .slug(slugify.slugify(childName))
                        .parent(parent)
                        .zk(zk)
                        .build());
            }
        }

        private void assertZkStates(Map<String, Boolean> zkStates) {
            zkStates.forEach((name, zk) -> {
                val institution = institutionRepository.findBySlug(slugify.slugify(name)).get();
                assertThat(name + ".zk == " + zk.toString(), institution.isZk() == zk);
            });
        }

        @Nested
        class Set {
            private static final String ZK_SET_URL_TEMPLATE =
                    Constants.ADMIN_PREFIX + "/institutions/{slug}/zk/set";

            @Test
            public void shouldBeSecured() throws Exception {
                mockMvc.perform(patch(ZK_SET_URL_TEMPLATE, slugify.slugify("A"))
                        .with(jwtBearerToken(user, objectMapper)))
                        .andExpect(status().isForbidden());
            }

            @Test
            public void shouldReturn404IfNotFound() throws Exception {
                assertZkStates(Map.of(
                        "A",     false
                ));

                mockMvc.perform(patch(ZK_SET_URL_TEMPLATE, slugify.slugify("A-1"))
                        .with(jwtBearerToken(admin, objectMapper)))
                        .andExpect(status().isNotFound());

                assertZkStates(Map.of(
                        "A",     false
                ));
            }

            @Test
            public void shouldPropagateToSubtreeFromRoot() throws Exception {
                createHierarchy(false,
                        "A",   "A-1",
                        "A",   "A-2",
                        "A-1", "A-1-1"
                );

                assertZkStates(Map.of(
                        "A",     false,
                        "A-1",   false,
                        "A-2",   false,
                        "A-1-1", false
                ));

                mockMvc.perform(patch(ZK_SET_URL_TEMPLATE, slugify.slugify("A"))
                        .with(jwtBearerToken(admin, objectMapper)))
                        .andExpect(status().isOk());

                assertZkStates(Map.of(
                        "A",     true,
                        "A-1",   true,
                        "A-2",   true,
                        "A-1-1", true
                ));
            }

            @Test
            public void shouldPropagateToSubtree() throws Exception {
                createHierarchy(false,
                        "A",   "A-1",
                        "A",   "A-2",
                        "A-1", "A-1-1"
                );

                assertZkStates(Map.of(
                        "A",     false,
                        "A-1",   false,
                        "A-2",   false,
                        "A-1-1", false
                ));

                mockMvc.perform(patch(ZK_SET_URL_TEMPLATE, slugify.slugify("A-1"))
                        .with(jwtBearerToken(admin, objectMapper)))
                        .andExpect(status().isOk());

                assertZkStates(Map.of(
                        "A",     false,
                        "A-1",   true,
                        "A-2",   false,
                        "A-1-1", true
                ));
            }

            @Test
            public void shouldForbidIfSet() throws Exception {
                createHierarchy(true,
                        "A",   "A-1",
                        "A",   "A-2",
                        "A-1", "A-1-1"
                );

                assertZkStates(Map.of(
                        "A",     false,
                        "A-1",   true,
                        "A-2",   true,
                        "A-1-1", true
                ));

                mockMvc.perform(patch(ZK_SET_URL_TEMPLATE, slugify.slugify("A-1"))
                        .with(jwtBearerToken(admin, objectMapper)))
                        .andExpect(status().isBadRequest());

                assertZkStates(Map.of(
                        "A",     false,
                        "A-1",   true,
                        "A-2",   true,
                        "A-1-1", true
                ));
            }

            @Test
            public void shouldAllowToMoveZkRootUp() throws Exception {
                createHierarchy(true,
                        "A",   "A-1",
                        "A",   "A-2",
                        "A-1", "A-1-1"
                );

                assertZkStates(Map.of(
                        "A",     false,
                        "A-1",   true,
                        "A-2",   true,
                        "A-1-1", true
                ));

                mockMvc.perform(patch(ZK_SET_URL_TEMPLATE, slugify.slugify("A"))
                        .with(jwtBearerToken(admin, objectMapper)))
                        .andExpect(status().isOk());

                assertZkStates(Map.of(
                        "A",     true,
                        "A-1",   true,
                        "A-2",   true,
                        "A-1-1", true
                ));
            }
        }

        @Nested
        class Unset {

            private static final String ZK_UNSET_URL_TEMPLATE =
                    Constants.ADMIN_PREFIX + "/institutions/{slug}/zk/unset";

            @Test
            public void shouldBeSecured() throws Exception {
                mockMvc.perform(patch(ZK_UNSET_URL_TEMPLATE, slugify.slugify("A"))
                        .with(jwtBearerToken(user, objectMapper)))
                        .andExpect(status().isForbidden());
            }

            @Test
            public void shouldReturn404IfNotFound() throws Exception {
                institution.setZk(true);
                institutionRepository.save(institution);

                assertZkStates(Map.of(
                        "A",     true
                ));

                mockMvc.perform(patch(ZK_UNSET_URL_TEMPLATE, slugify.slugify("A-1"))
                        .with(jwtBearerToken(admin, objectMapper)))
                        .andExpect(status().isNotFound());

                assertZkStates(Map.of(
                        "A",     true
                ));
            }

            @Test
            public void shouldPropagateToSubtreeFromRoot() throws Exception {
                institution.setZk(true);
                institutionRepository.save(institution);

                createHierarchy(true,
                        "A",   "A-1",
                        "A",   "A-2",
                        "A-1", "A-1-1"
                );

                assertZkStates(Map.of(
                        "A",     true,
                        "A-1",   true,
                        "A-2",   true,
                        "A-1-1", true
                ));

                mockMvc.perform(patch(ZK_UNSET_URL_TEMPLATE, slugify.slugify("A"))
                        .with(jwtBearerToken(admin, objectMapper)))
                        .andExpect(status().isOk());

                assertZkStates(Map.of(
                        "A",     false,
                        "A-1",   false,
                        "A-2",   false,
                        "A-1-1", false
                ));
            }

            @Test
            public void shouldPropagateToSubtree() throws Exception {
                createHierarchy(true,
                        "A",   "A-1",
                        "A",   "A-2",
                        "A-1", "A-1-1"
                );

                assertZkStates(Map.of(
                        "A",     false,
                        "A-1",   true,
                        "A-2",   true,
                        "A-1-1", true
                ));

                mockMvc.perform(patch(ZK_UNSET_URL_TEMPLATE, slugify.slugify("A-1"))
                        .with(jwtBearerToken(admin, objectMapper)))
                        .andExpect(status().isOk());

                assertZkStates(Map.of(
                        "A",     false,
                        "A-1",   false,
                        "A-2",   true,
                        "A-1-1", false
                ));
            }

            @Test
            public void shouldForbidIfNotZkRoot() throws Exception {
                institution.setZk(true);
                institutionRepository.save(institution);

                createHierarchy(true,
                        "A",   "A-1",
                        "A",   "A-2",
                        "A-1", "A-1-1"
                );

                assertZkStates(Map.of(
                        "A",     true,
                        "A-1",   true,
                        "A-2",   true,
                        "A-1-1", true
                ));

                mockMvc.perform(patch(ZK_UNSET_URL_TEMPLATE, slugify.slugify("A-1"))
                        .with(jwtBearerToken(admin, objectMapper)))
                        .andExpect(status().isBadRequest());

                assertZkStates(Map.of(
                        "A",     true,
                        "A-1",   true,
                        "A-2",   true,
                        "A-1-1", true
                ));
            }

            @Test
            public void shouldForbidToUnsetIfNotSet() throws Exception {
                assertZkStates(Map.of(
                        "A",     false
                ));

                mockMvc.perform(patch(ZK_UNSET_URL_TEMPLATE, slugify.slugify("A"))
                        .with(jwtBearerToken(admin, objectMapper)))
                        .andExpect(status().isBadRequest());

                assertZkStates(Map.of(
                        "A",     false
                ));
            }
        }
    }
}
