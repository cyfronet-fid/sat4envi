package pl.cyfronet.s4e.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import pl.cyfronet.s4e.BasicTest;
import pl.cyfronet.s4e.TestDbHelper;
import pl.cyfronet.s4e.bean.AppUser;
import pl.cyfronet.s4e.controller.request.LoginRequest;
import pl.cyfronet.s4e.data.repository.AppUserRepository;
import pl.cyfronet.s4e.security.SecurityConstants;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static pl.cyfronet.s4e.Constants.API_PREFIX_V1;

@AutoConfigureMockMvc
@BasicTest
class AuthControllerTest {
    /// Constant in all our tokens.
    private static final String JWT_ALG_PREFIX = "eyJhbGciOiJSUzI1NiJ9";

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

        String cookieName = SecurityConstants.COOKIE_NAME;

        mockMvc.perform(post(API_PREFIX_V1+"/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(cookie().value(cookieName, startsWith(JWT_ALG_PREFIX)))
                .andExpect(cookie().httpOnly(cookieName, true))
                .andExpect(cookie().path(cookieName, "/"))
                .andExpect(cookie().secure(cookieName, true))
                .andExpect(cookie().maxAge(cookieName, greaterThan(0)))
                .andExpect(content().string(is(emptyOrNullString())));
    }

    @Test
    public void loginShouldReturn403ForDisabledAccount() throws Exception {
        val appUser = appUserRepository.save(appUser(false));

        val loginRequest = LoginRequest.builder()
                .email(appUser.getEmail())
                .password("password")
                .build();

        mockMvc.perform(post(API_PREFIX_V1+"/login")
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
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(loginRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void logoutShouldResetCookie() throws Exception {
        String cookieName = SecurityConstants.COOKIE_NAME;

        mockMvc.perform(post(API_PREFIX_V1+"/logout")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(cookie().value(cookieName, is(emptyOrNullString())))
                .andExpect(cookie().httpOnly(cookieName, true))
                .andExpect(cookie().path(cookieName, "/"))
                .andExpect(cookie().secure(cookieName, true))
                .andExpect(cookie().maxAge(cookieName, 0)) // resets cookie
                .andExpect(content().string(is(emptyOrNullString())));
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
