package pl.cyfronet.s4e.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import pl.cyfronet.s4e.BasicTest;
import pl.cyfronet.s4e.TestDbHelper;
import pl.cyfronet.s4e.bean.AppUser;
import pl.cyfronet.s4e.controller.request.UserAuthorityRequest;
import pl.cyfronet.s4e.data.repository.AppUserRepository;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.ResultMatcher.matchAll;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static pl.cyfronet.s4e.Constants.API_PREFIX_V1;
import static pl.cyfronet.s4e.TestJwtUtil.jwtBearerToken;

@BasicTest
@AutoConfigureMockMvc
class UserAuthorityControllerTest {
    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TestDbHelper testDbHelper;

    @BeforeEach
    public void beforeEach() {
        testDbHelper.clean();

        appUserRepository.save(AppUser.builder()
                .email("admin@mail.pl")
                .name("Name")
                .surname("Surname")
                .password("{noop}password")
                .enabled(true)
                .authority("ROLE_ADMIN")
                .build());

        appUserRepository.save(AppUser.builder()
                .email("granter_A@mail.pl")
                .name("Name")
                .surname("Surname")
                .password("{noop}password")
                .enabled(true)
                .authority("OP_GRANT_A")
                .build());

        appUserRepository.save(AppUser.builder()
                .email("granter_B@mail.pl")
                .name("Name")
                .surname("Surname")
                .password("{noop}password")
                .enabled(true)
                .authority("OP_GRANT_B")
                .build());

        appUserRepository.save(AppUser.builder()
                .email("authority_A@mail.pl")
                .name("Name")
                .surname("Surname")
                .password("{noop}password")
                .enabled(true)
                .authority("A")
                .build());

        appUserRepository.save(AppUser.builder()
                .email("authority_B@mail.pl")
                .name("Name")
                .surname("Surname")
                .password("{noop}password")
                .enabled(true)
                .authority("B")
                .build());

        appUserRepository.save(AppUser.builder()
                .email("user@mail.pl")
                .name("Name")
                .surname("Surname")
                .password("{noop}password")
                .enabled(true)
                .build());
    }

    private AppUser user(String name) {
        return appUserRepository.findByEmail(name + "@mail.pl").get();
    }

    @Nested
    class ListEndpoint {
        private static final String URL_TEMPLATE = API_PREFIX_V1 + "/users/authority/{authority}";

        @ParameterizedTest
        @ValueSource(strings = { "admin", "granter_A" })
        public void shouldAllowForAuthA(String userName) throws Exception {
            mockMvc.perform(get(URL_TEMPLATE, "A")
                    .with(jwtBearerToken(user(userName), objectMapper)))
                    .andExpect(matchAll(
                            status().isOk(),
                            jsonPath("$", hasSize(1)),
                            jsonPath("$[0].email", is(equalTo("authority_A@mail.pl")))
                    ));
        }

        @ParameterizedTest
        @ValueSource(strings = { "granter_B", "authority_A", "authority_B", "user" })
        public void shouldForbidForAuthA(String userName) throws Exception {
            mockMvc.perform(get(URL_TEMPLATE, "A")
                    .with(jwtBearerToken(user(userName), objectMapper)))
                    .andExpect(status().isForbidden());
        }

        @ParameterizedTest
        @ValueSource(strings = { "admin" })
        public void shouldAllowForAuthOther(String userName) throws Exception {
            mockMvc.perform(get(URL_TEMPLATE, "OTHER")
                    .with(jwtBearerToken(user(userName), objectMapper)))
                    .andExpect(matchAll(
                            status().isOk(),
                            jsonPath("$", hasSize(0))
                    ));
        }

        @ParameterizedTest
        @ValueSource(strings = { "granter_A", "granter_B", "authority_A", "authority_B", "user" })
        public void shouldForbidForAuthOther(String userName) throws Exception {
            mockMvc.perform(get(URL_TEMPLATE, "OTHER")
                    .with(jwtBearerToken(user(userName), objectMapper)))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    class AddEndpoint {
        private static final String URL_TEMPLATE = API_PREFIX_V1 + "/users/authority/{authority}";

        @ParameterizedTest
        @ValueSource(strings = { "admin", "granter_A" })
        public void shouldAllowForAuthA(String userName) throws Exception {
            UserAuthorityRequest request = new UserAuthorityRequest("authority_B@mail.pl");

            assertThat(user("authority_B").getAuthorities(), containsInAnyOrder("B"));

            mockMvc.perform(put(URL_TEMPLATE, "A")
                    .contentType(MediaType.APPLICATION_JSON)
                    .with(jwtBearerToken(user(userName), objectMapper))
                    .content(objectMapper.writeValueAsBytes(request)))
                    .andExpect(status().isOk());

            assertThat(user("authority_B").getAuthorities(), containsInAnyOrder("A", "B"));
        }

        @ParameterizedTest
        @ValueSource(strings = { "granter_B", "authority_A", "authority_B", "user" })
        public void shouldForbidForAuthA(String userName) throws Exception {
            UserAuthorityRequest request = new UserAuthorityRequest("authority_B@mail.pl");

            assertThat(user("authority_B").getAuthorities(), containsInAnyOrder("B"));

            mockMvc.perform(put(URL_TEMPLATE, "A")
                    .contentType(MediaType.APPLICATION_JSON)
                    .with(jwtBearerToken(user(userName), objectMapper))
                    .content(objectMapper.writeValueAsBytes(request)))
                    .andExpect(status().isForbidden());

            assertThat(user("authority_B").getAuthorities(), containsInAnyOrder("B"));
        }

        @ParameterizedTest
        @ValueSource(strings = { "admin", "granter_A" })
        public void shouldReturn404ForNonExistingUser(String userName) throws Exception {
            UserAuthorityRequest request = new UserAuthorityRequest("doesntExist@mail.pl");

            mockMvc.perform(put(URL_TEMPLATE, "A")
                    .contentType(MediaType.APPLICATION_JSON)
                    .with(jwtBearerToken(user(userName), objectMapper))
                    .content(objectMapper.writeValueAsBytes(request)))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    class RemoveEndpoint {
        private static final String URL_TEMPLATE = API_PREFIX_V1 + "/users/authority/{authority}";

        @ParameterizedTest
        @ValueSource(strings = { "admin", "granter_A" })
        public void shouldAllowForAuthA(String userName) throws Exception {
            UserAuthorityRequest request = new UserAuthorityRequest("authority_A@mail.pl");

            assertThat(user("authority_A").getAuthorities(), containsInAnyOrder("A"));

            mockMvc.perform(delete(URL_TEMPLATE, "A")
                    .contentType(MediaType.APPLICATION_JSON)
                    .with(jwtBearerToken(user(userName), objectMapper))
                    .content(objectMapper.writeValueAsBytes(request)))
                    .andExpect(status().isOk());

            assertThat(user("authority_A").getAuthorities(), hasSize(0));
        }

        @ParameterizedTest
        @ValueSource(strings = { "granter_B", "authority_A", "authority_B", "user" })
        public void shouldForbidForAuthA(String userName) throws Exception {
            UserAuthorityRequest request = new UserAuthorityRequest("authority_A@mail.pl");

            assertThat(user("authority_A").getAuthorities(), containsInAnyOrder("A"));

            mockMvc.perform(delete(URL_TEMPLATE, "A")
                    .contentType(MediaType.APPLICATION_JSON)
                    .with(jwtBearerToken(user(userName), objectMapper))
                    .content(objectMapper.writeValueAsBytes(request)))
                    .andExpect(status().isForbidden());

            assertThat(user("authority_A").getAuthorities(), containsInAnyOrder("A"));
        }

        @ParameterizedTest
        @ValueSource(strings = { "admin", "granter_A" })
        public void shouldReturn404ForNonExistingUser(String userName) throws Exception {
            UserAuthorityRequest request = new UserAuthorityRequest("doesntExist@mail.pl");

            mockMvc.perform(delete(URL_TEMPLATE, "A")
                    .contentType(MediaType.APPLICATION_JSON)
                    .with(jwtBearerToken(user(userName), objectMapper))
                    .content(objectMapper.writeValueAsBytes(request)))
                    .andExpect(status().isNotFound());
        }
    }

}
