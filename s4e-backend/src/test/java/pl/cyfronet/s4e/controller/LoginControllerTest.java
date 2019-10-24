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
import pl.cyfronet.s4e.bean.AppUser;
import pl.cyfronet.s4e.controller.request.LoginRequest;
import pl.cyfronet.s4e.data.repository.AppUserRepository;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static pl.cyfronet.s4e.Constants.API_PREFIX_V1;

@AutoConfigureMockMvc
@BasicTest
class LoginControllerTest {
    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    public void beforeEach() {
        appUserRepository.deleteAll();
    }

    @Test
    public void shouldReturnJWSToken() throws Exception {
        val appUser = appUserRepository.save(AppUser.builder()
                .email("some@email.com")
                .password(passwordEncoder.encode("password"))
                .enabled(true)
                .build());

        val loginRequest = LoginRequest.builder()
                .email(appUser.getEmail())
                .password("password")
                .build();

        mockMvc.perform(post(API_PREFIX_V1+"/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(loginRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("email", is(equalTo("some@email.com"))))
            .andExpect(jsonPath("token", is(not(isEmptyOrNullString()))));
    }

    @Test
    public void shouldReturn403ForDisabledAccount() throws Exception {
        val appUser = appUserRepository.save(AppUser.builder()
                .email("some@email.com")
                .password(passwordEncoder.encode("password"))
                .enabled(false)
                .build());

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
    public void shouldReturn401ForIncorrectCredentials() throws Exception {
        val appUser = appUserRepository.save(AppUser.builder()
                .email("some@email.com")
                .password(passwordEncoder.encode("password"))
                .enabled(true)
                .build());

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
    public void shouldReturn401ForNonexistentAccount() throws Exception {
        val loginRequest = LoginRequest.builder()
                .email("does@not.exist")
                .password("password")
                .build();

        mockMvc.perform(post(API_PREFIX_V1+"/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(loginRequest)))
                .andExpect(status().isUnauthorized());
    }
}
