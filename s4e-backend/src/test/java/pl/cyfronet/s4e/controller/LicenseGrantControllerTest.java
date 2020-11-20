package pl.cyfronet.s4e.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import pl.cyfronet.s4e.BasicTest;
import pl.cyfronet.s4e.Constants;
import pl.cyfronet.s4e.SceneTestHelper;
import pl.cyfronet.s4e.TestDbHelper;
import pl.cyfronet.s4e.bean.*;
import pl.cyfronet.s4e.data.repository.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.ResultMatcher.matchAll;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static pl.cyfronet.s4e.TestJwtUtil.jwtBearerToken;

@BasicTest
@AutoConfigureMockMvc
public class LicenseGrantControllerTest {
    private static final String URL_PREFIX = Constants.API_PREFIX_V1 + "/license-grants";

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private InstitutionRepository institutionRepository;

    @Autowired
    private UserRoleRepository userRoleRepository;

    @Autowired
    private LicenseGrantRepository licenseGrantRepository;

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private TestDbHelper testDbHelper;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    private Product product1;
    private Institution instA;
    private Institution instB;

    @BeforeEach
    public void beforeEach() {
        testDbHelper.clean();

        product1 = productRepository.save(SceneTestHelper.productBuilder()
                .accessType(Product.AccessType.PRIVATE)
                .build());

        instA = institutionRepository.save(Institution.builder()
                .name("A")
                .slug("A")
                .build());

        instB = institutionRepository.save(Institution.builder()
                .name("B")
                .slug("B")
                .build());

        licenseGrantRepository.save(LicenseGrant.builder()
                .product(product1)
                .institution(instA)
                .owner(true)
                .build());

        val instAdmin = appUserRepository.save(AppUser.builder()
                .email("instAdmin@mail.pl")
                .name("John")
                .surname("Smith")
                .password("{noop}password")
                .enabled(true)
                .build());

        userRoleRepository.save(UserRole.builder()
                .role(AppRole.INST_MEMBER)
                .user(instAdmin)
                .institution(instA)
                .build());
        userRoleRepository.save(UserRole.builder()
                .role(AppRole.INST_ADMIN)
                .user(instAdmin)
                .institution(instA)
                .build());

        val instMember = appUserRepository.save(AppUser.builder()
                .email("instMember@mail.pl")
                .name("John")
                .surname("Smith")
                .password("{noop}password")
                .enabled(true)
                .build());

        userRoleRepository.save(UserRole.builder()
                .role(AppRole.INST_MEMBER)
                .user(instMember)
                .institution(instA)
                .build());

        appUserRepository.save(AppUser.builder()
                .email("user@mail.pl")
                .name("John")
                .surname("Smith")
                .password("{noop}password")
                .enabled(true)
                .build());

        appUserRepository.save(AppUser.builder()
                .email("admin@mail.pl")
                .name("John")
                .surname("Smith")
                .password("{noop}password")
                .enabled(true)
                .admin(true)
                .build());
    }

    private AppUser user(String name) {
        return appUserRepository.findByEmail(name + "@mail.pl").get();
    }

    @Nested
    class ListByInstitution {
        private static final String URL_TEMPLATE = URL_PREFIX + "/institution/{institutionSlug}";
        private Product product2;

        @BeforeEach
        public void beforeEach() {
            product2 = productRepository.save(SceneTestHelper.productBuilder()
                    .accessType(Product.AccessType.PRIVATE)
                    .build());

            licenseGrantRepository.save(LicenseGrant.builder()
                    .institution(instA)
                    .product(product2)
                    .build());

            // This LicenseGrant is meant not to be included in the responses.
            licenseGrantRepository.save(LicenseGrant.builder()
                    .institution(instB)
                    .product(product2)
                    .build());
        }

        @ParameterizedTest
        @ValueSource(strings = { "admin", "instAdmin", "instMember" })
        public void shouldAllowForInstA(String userName) throws Exception {
            mockMvc.perform(get(URL_TEMPLATE, "A")
                    .with(jwtBearerToken(user(userName), objectMapper)))
                    .andExpect(matchAll(
                            status().isOk(),
                            jsonPath("$", hasSize(2)),
                            jsonPath("$[0].institutionSlug", is(equalTo("A"))),
                            jsonPath("$[0].productId", is(equalTo(product1.getId().intValue()))),
                            jsonPath("$[0].owner", is(equalTo(true))),
                            jsonPath("$[1].institutionSlug", is(equalTo("A"))),
                            jsonPath("$[1].productId", is(equalTo(product2.getId().intValue()))),
                            jsonPath("$[1].owner", is(equalTo(false)))
                    ));
        }

        @ParameterizedTest
        @ValueSource(strings = { "user" })
        public void shouldForbidForInstA(String userName) throws Exception {
            mockMvc.perform(get(URL_TEMPLATE, "A")
                    .with(jwtBearerToken(user(userName), objectMapper)))
                    .andExpect(status().isForbidden());
        }

        @ParameterizedTest
        @ValueSource(strings = { "instAdmin", "instMember", "user" })
        public void shouldForbidForInstB(String userName) throws Exception {
            mockMvc.perform(get(URL_TEMPLATE, "B")
                    .with(jwtBearerToken(user(userName), objectMapper)))
                    .andExpect(status().isForbidden());
        }

        @ParameterizedTest
        @ValueSource(strings = { "instAdmin", "instMember", "user" })
        public void shouldReturn403ForNonExistentInstitution(String userName) throws Exception {
            mockMvc.perform(get(URL_TEMPLATE, "doesnt_exist")
                    .with(jwtBearerToken(user(userName), objectMapper)))
                    .andExpect(status().isForbidden());
        }

        @ParameterizedTest
        @ValueSource(strings = { "admin" })
        public void shouldReturn404ForNonExistentInstitution(String userName) throws Exception {
            mockMvc.perform(get(URL_TEMPLATE, "doesnt_exist")
                    .with(jwtBearerToken(user(userName), objectMapper)))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    class ListByProduct {
        private static final String URL_TEMPLATE = URL_PREFIX + "/product/{productId}";
        private Product product2;

        @BeforeEach
        public void beforeEach() {
            product2 = productRepository.save(SceneTestHelper.productBuilder()
                    .accessType(Product.AccessType.PRIVATE)
                    .build());

            licenseGrantRepository.save(LicenseGrant.builder()
                    .institution(instB)
                    .product(product1)
                    .build());
        }

        @ParameterizedTest
        @ValueSource(strings = { "admin", "instAdmin" })
        public void shouldAllowForProduct1(String userName) throws Exception {
            mockMvc.perform(get(URL_TEMPLATE, product1.getId())
                    .with(jwtBearerToken(user(userName), objectMapper)))
                    .andExpect(matchAll(
                            status().isOk(),
                            jsonPath("$", hasSize(2)),
                            jsonPath("$[0].institutionSlug", is(equalTo("A"))),
                            jsonPath("$[0].productId", is(equalTo(product1.getId().intValue()))),
                            jsonPath("$[0].owner", is(equalTo(true))),
                            jsonPath("$[1].institutionSlug", is(equalTo("B"))),
                            jsonPath("$[1].productId", is(equalTo(product1.getId().intValue()))),
                            jsonPath("$[1].owner", is(equalTo(false)))
                    ));
        }

        @ParameterizedTest
        @ValueSource(strings = { "instMember", "user" })
        public void shouldForbidForProduct1(String userName) throws Exception {
            mockMvc.perform(get(URL_TEMPLATE, product1.getId())
                    .with(jwtBearerToken(user(userName), objectMapper)))
                    .andExpect(status().isForbidden());
        }

        @ParameterizedTest
        @ValueSource(strings = { "admin" })
        public void shouldAllowForProduct2(String userName) throws Exception {
            mockMvc.perform(get(URL_TEMPLATE, product2.getId())
                    .with(jwtBearerToken(user(userName), objectMapper)))
                    .andExpect(matchAll(
                            status().isOk(),
                            jsonPath("$", hasSize(0))
                    ));
        }

        @ParameterizedTest
        @ValueSource(strings = { "instAdmin", "instMember", "user" })
        public void shouldForbidForProduct2(String userName) throws Exception {
            mockMvc.perform(get(URL_TEMPLATE, product2.getId())
                    .with(jwtBearerToken(user(userName), objectMapper)))
                    .andExpect(status().isForbidden());
        }

        @ParameterizedTest
        @ValueSource(strings = { "admin", "instAdmin", "instMember", "user" })
        public void shouldReturn403ForNonExistentProduct(String userName) throws Exception {
            mockMvc.perform(get(URL_TEMPLATE, product2.getId() + 1)
                    .with(jwtBearerToken(user(userName), objectMapper)))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    class GrantAccess {
        private static final String URL_TEMPLATE = URL_PREFIX + "/product/{productId}/institution/{institutionSlug}";

        @ParameterizedTest
        @ValueSource(strings = { "admin", "instAdmin" })
        public void shouldAllow(String userName) throws Exception {
            assertThat(licenseGrantRepository.count(), is(equalTo(1L)));

            mockMvc.perform(post(URL_TEMPLATE, product1.getId(), "B")
                    .with(jwtBearerToken(user(userName), objectMapper)))
                    .andExpect(matchAll(
                            status().isOk(),
                            jsonPath("$.institutionSlug", is(equalTo("B"))),
                            jsonPath("$.productId", is(equalTo(product1.getId().intValue()))),
                            jsonPath("$.owner", is(equalTo(false)))
                    ));

            assertThat(licenseGrantRepository.count(), is(equalTo(2L)));
        }

        @ParameterizedTest
        @ValueSource(strings = { "instMember", "user" })
        public void shouldForbid(String userName) throws Exception {
            assertThat(licenseGrantRepository.count(), is(equalTo(1L)));

            mockMvc.perform(post(URL_TEMPLATE, product1.getId(), "B")
                    .with(jwtBearerToken(user(userName), objectMapper)))
                    .andExpect(status().isForbidden());

            assertThat(licenseGrantRepository.count(), is(equalTo(1L)));
        }

        @ParameterizedTest
        @ValueSource(strings = { "admin", "instAdmin" })
        public void shouldRequireValidInstitutionSlug(String userName) throws Exception {
            assertThat(licenseGrantRepository.count(), is(equalTo(1L)));

            mockMvc.perform(post(URL_TEMPLATE, product1.getId(), "C")
                    .with(jwtBearerToken(user(userName), objectMapper)))
                    .andExpect(status().isBadRequest());

            assertThat(licenseGrantRepository.count(), is(equalTo(1L)));
        }

        @Nested
        class WithPreexistingGrant {
            @BeforeEach
            public void beforeEach() {
                licenseGrantRepository.save(LicenseGrant.builder()
                        .institution(instB)
                        .product(product1)
                        .build());
            }

            @ParameterizedTest
            @CsvSource({
                    "admin,A",
                    "admin,B",
                    "instAdmin,A",
                    "instAdmin,B"
            })
            public void shouldRequireNoPreexistingGrant(String userName, String institutionSlug) throws Exception {
                assertThat(licenseGrantRepository.count(), is(equalTo(2L)));

                mockMvc.perform(post(URL_TEMPLATE, product1.getId(), institutionSlug)
                        .with(jwtBearerToken(user(userName), objectMapper)))
                        .andExpect(status().isBadRequest());

                assertThat(licenseGrantRepository.count(), is(equalTo(2L)));
            }
        }
    }

    @Nested
    class DenyAccess {
        private static final String URL_TEMPLATE = URL_PREFIX + "/product/{productId}/institution/{institutionSlug}";
        private Product product2;

        @BeforeEach
        public void beforeEach() {
            product2 = productRepository.save(SceneTestHelper.productBuilder()
                    .accessType(Product.AccessType.PRIVATE)
                    .build());
        }

        @ParameterizedTest
        @ValueSource(strings = { "admin", "instAdmin" })
        public void shouldForbidToDeleteOwnerGrant(String userName) throws Exception {
            assertThat(licenseGrantRepository.count(), is(equalTo(1L)));

            mockMvc.perform(delete(URL_TEMPLATE, product1.getId(), "A")
                    .with(jwtBearerToken(user(userName), objectMapper)))
                    .andExpect(status().isBadRequest());

            assertThat(licenseGrantRepository.count(), is(equalTo(1L)));
        }

        @ParameterizedTest
        @ValueSource(strings = { "instMember", "user" })
        public void shouldForbidForProduct1(String userName) throws Exception {
            assertThat(licenseGrantRepository.count(), is(equalTo(1L)));

            mockMvc.perform(delete(URL_TEMPLATE, product1.getId(), "A")
                    .with(jwtBearerToken(user(userName), objectMapper)))
                    .andExpect(status().isForbidden());

            assertThat(licenseGrantRepository.count(), is(equalTo(1L)));
        }

        @ParameterizedTest
        @ValueSource(strings = { "instAdmin", "instMember", "user" })
        public void shouldForbidForProduct2(String userName) throws Exception {
            assertThat(licenseGrantRepository.count(), is(equalTo(1L)));

            mockMvc.perform(delete(URL_TEMPLATE, product2.getId(), "A")
                    .with(jwtBearerToken(user(userName), objectMapper)))
                    .andExpect(status().isForbidden());

            assertThat(licenseGrantRepository.count(), is(equalTo(1L)));
        }

        @ParameterizedTest
        @ValueSource(strings = { "admin", "instAdmin" })
        public void shouldRequireExistingInstitutionSlug(String userName) throws Exception {
            assertThat(licenseGrantRepository.count(), is(equalTo(1L)));

            mockMvc.perform(delete(URL_TEMPLATE, product1.getId(), "B")
                    .with(jwtBearerToken(user(userName), objectMapper)))
                    .andExpect(status().isNotFound());

            assertThat(licenseGrantRepository.count(), is(equalTo(1L)));
        }

        @Nested
        class WithPreexistingGrant {
            @BeforeEach
            public void beforeEach() {
                licenseGrantRepository.save(LicenseGrant.builder()
                        .institution(instB)
                        .product(product1)
                        .build());
            }

            @ParameterizedTest
            @ValueSource(strings = { "admin", "instAdmin" })
            public void shouldAllowForProduct1(String userName) throws Exception {
                assertThat(licenseGrantRepository.count(), is(equalTo(2L)));

                mockMvc.perform(delete(URL_TEMPLATE, product1.getId(), "B")
                        .with(jwtBearerToken(user(userName), objectMapper)))
                        .andExpect(status().isNoContent());

                assertThat(licenseGrantRepository.count(), is(equalTo(1L)));
            }
        }
    }
}
