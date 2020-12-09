package pl.cyfronet.s4e.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;
import pl.cyfronet.s4e.BasicTest;
import pl.cyfronet.s4e.TestDbHelper;
import pl.cyfronet.s4e.bean.AppUser;
import pl.cyfronet.s4e.controller.request.LoginRequest;
import pl.cyfronet.s4e.data.repository.AppUserRepository;
import pl.cyfronet.s4e.security.SecurityConstants;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.ResultMatcher.matchAll;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static pl.cyfronet.s4e.Constants.API_PREFIX_V1;

@AutoConfigureMockMvc
@BasicTest
class AuthControllerTest {
    /// Constant in all our tokens.
    private static final String JWT_ALG_PREFIX = "eyJhbGciOiJSUzI1NiJ9";
    private static final String TOKEN_COOKIE = SecurityConstants.COOKIE_NAME;
    private static final String TEST_HOST = "test-host:1234";
    private static final String TEST_HOST_DOMAIN = "test-host";

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TestDbHelper testDbHelper;

    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    public void beforeEach() {
        testDbHelper.clean();
    }

    @Test
    public void tokenShouldReturnJWS() throws Exception {
        val appUser = appUserRepository.save(appUser(true));

        val loginRequest = LoginRequest.builder()
                .email(appUser.getEmail())
                .password("password")
                .build();

        mockMvc.perform(post(API_PREFIX_V1+"/token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(loginRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("email", is(equalTo("some@email.com"))))
            .andExpect(jsonPath("token", startsWith(JWT_ALG_PREFIX)));
    }

    @Test
    public void tokenShouldReturn403ForDisabledAccount() throws Exception {
        val appUser = appUserRepository.save(appUser(false));

        val loginRequest = LoginRequest.builder()
                .email(appUser.getEmail())
                .password("password")
                .build();

        mockMvc.perform(post(API_PREFIX_V1+"/token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(loginRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    public void tokenShouldReturn401ForIncorrectCredentials() throws Exception {
        val appUser = appUserRepository.save(appUser(true));

        val loginRequest = LoginRequest.builder()
                .email(appUser.getEmail())
                .password("incorrectPassword")
                .build();

        mockMvc.perform(post(API_PREFIX_V1+"/token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(loginRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void tokenShouldReturn401ForNonexistentAccount() throws Exception {
        val loginRequest = LoginRequest.builder()
                .email("does@not.exist")
                .password("password")
                .build();

        mockMvc.perform(post(API_PREFIX_V1+"/token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(loginRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void loginShouldSetCookie() throws Exception {
        val appUser = appUserRepository.save(appUser(true));

        val loginRequest = LoginRequest.builder()
                .email(appUser.getEmail())
                .password("password")
                .build();

        mockMvc.perform(post(API_PREFIX_V1+"/login")
                .header("Host", TEST_HOST)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(content().string(is(emptyOrNullString())))
                .andExpect(setsCookie(TEST_HOST_DOMAIN));
    }

    @Test
    public void loginShouldSetCookieWithXForwardedHost() throws Exception {
        val appUser = appUserRepository.save(appUser(true));

        val loginRequest = LoginRequest.builder()
                .email(appUser.getEmail())
                .password("password")
                .build();

        mockMvc.perform(post(API_PREFIX_V1+"/login")
                .header("Host", TEST_HOST)
                .header("X-Forwarded-Host", "original-host")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(content().string(is(emptyOrNullString())))
                .andExpect(setsCookie("original-host"));
    }

    @Test
    public void loginShouldReturn403ForDisabledAccount() throws Exception {
        val appUser = appUserRepository.save(appUser(false));

        val loginRequest = LoginRequest.builder()
                .email(appUser.getEmail())
                .password("password")
                .build();

        mockMvc.perform(post(API_PREFIX_V1+"/login")
                .header("Host", TEST_HOST)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(loginRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    public void loginShouldReturn401ForIncorrectCredentials() throws Exception {
        val appUser = appUserRepository.save(appUser(true));

        val loginRequest = LoginRequest.builder()
                .email(appUser.getEmail())
                .password("incorrectPassword")
                .build();

        mockMvc.perform(post(API_PREFIX_V1+"/login")
                .header("Host", TEST_HOST)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(loginRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void loginShouldReturn401ForNonexistentAccount() throws Exception {
        val loginRequest = LoginRequest.builder()
                .email("does@not.exist")
                .password("password")
                .build();

        mockMvc.perform(post(API_PREFIX_V1+"/login")
                .header("Host", TEST_HOST)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(loginRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void logoutShouldResetCookie() throws Exception {
        mockMvc.perform(post(API_PREFIX_V1+"/logout")
                .header("Host", TEST_HOST)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(is(emptyOrNullString())))
                .andExpect(unsetsCookie(TEST_HOST_DOMAIN));
    }

    @Test
    public void logoutShouldResetCookieWithXForwardedHost() throws Exception {
        mockMvc.perform(post(API_PREFIX_V1+"/logout")
                .header("Host", TEST_HOST)
                .header("X-Forwarded-Host", "original-host")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(is(emptyOrNullString())))
                .andExpect(unsetsCookie("original-host"));
    }

    @Nested
    @TestPropertySource(properties = {
            "jwt.cookie.domain=other-domain"
    })
    class WithCookieDomainProperty {
        // For some reason, the set property isn't propagated to the mockMvc from an enclosing class,
        // but works when it's moved here.
        @Autowired
        private MockMvc mockMvc;

        @Test
        public void loginShouldSetCookie() throws Exception {
            val appUser = appUserRepository.save(appUser(true));

            val loginRequest = LoginRequest.builder()
                    .email(appUser.getEmail())
                    .password("password")
                    .build();

            mockMvc.perform(post(API_PREFIX_V1+"/login")
                    .header("Host", TEST_HOST)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsBytes(loginRequest)))
                    .andExpect(status().isOk())
                    .andExpect(content().string(is(emptyOrNullString())))
                    .andExpect(setsCookie("other-domain"));
        }

        @Test
        public void logoutShouldResetCookie() throws Exception {
            mockMvc.perform(post(API_PREFIX_V1+"/logout")
                    .header("Host", TEST_HOST)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().string(is(emptyOrNullString())))
                    .andExpect(unsetsCookie("other-domain"));
        }
    }

    private ResultMatcher setsCookie(String expectedDomain) {
        return matchAll(
                cookie().value(TOKEN_COOKIE, startsWith(JWT_ALG_PREFIX)),
                cookie().httpOnly(TOKEN_COOKIE, true),
                cookie().path(TOKEN_COOKIE, "/"),
                cookie().secure(TOKEN_COOKIE, true),
                cookie().maxAge(TOKEN_COOKIE, greaterThan(0)),
                cookie().domain(TOKEN_COOKIE, expectedDomain)
        );
    }

    private ResultMatcher unsetsCookie(String expectedDomain) {
        return matchAll(
                cookie().value(TOKEN_COOKIE, is(emptyOrNullString())),
                cookie().httpOnly(TOKEN_COOKIE, true),
                cookie().path(TOKEN_COOKIE, "/"),
                cookie().secure(TOKEN_COOKIE, true),
                cookie().maxAge(TOKEN_COOKIE, 0), // resets cookie
                cookie().domain(TOKEN_COOKIE, expectedDomain)
        );
    }

    private AppUser appUser(boolean enabled) {
        return AppUser.builder()
                .email("some@email.com")
                .name("Name")
                .surname("Surname")
                .password(passwordEncoder.encode("password"))
                .enabled(enabled)
                .build();
    }
}
