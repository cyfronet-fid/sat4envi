package pl.cyfronet.s4e.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.icegreen.greenmail.util.GreenMail;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.event.EventListener;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.test.web.servlet.MockMvc;
import pl.cyfronet.s4e.BasicTest;
import pl.cyfronet.s4e.GreenMailSupplier;
import pl.cyfronet.s4e.TestDbHelper;
import pl.cyfronet.s4e.bean.AppUser;
import pl.cyfronet.s4e.bean.PasswordReset;
import pl.cyfronet.s4e.controller.request.PasswordChangeRequest;
import pl.cyfronet.s4e.controller.request.PasswordResetRequest;
import pl.cyfronet.s4e.data.repository.AppUserRepository;
import pl.cyfronet.s4e.data.repository.PasswordResetRepository;
import pl.cyfronet.s4e.event.OnPasswordResetTokenEmailEvent;
import pl.cyfronet.s4e.service.PasswordService;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAmount;
import java.util.UUID;

import static com.github.npathai.hamcrestopt.OptionalMatchers.isEmpty;
import static com.github.npathai.hamcrestopt.OptionalMatchers.isPresent;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static pl.cyfronet.s4e.Constants.API_PREFIX_V1;
import static pl.cyfronet.s4e.TestJwtUtil.jwtBearerToken;

@BasicTest
@Slf4j
@AutoConfigureMockMvc
public class PasswordControllerTest {
    private static final String PROFILE_EMAIL = "get@profile.com";
    private static final String WRONG_EMAIL = "mail@email.pl";
    private static final TemporalAmount EXPIRE_IN = Duration.ofDays(1);

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private PasswordResetRepository passwordResetRepository;

    @Autowired
    private PasswordService passwordService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private TestDbHelper testDbHelper;

    @Autowired
    private ObjectMapper objectMapper;

    @SpyBean
    private TestListener testListener;

    @Autowired
    private MockMvc mockMvc;

    private AppUser securityAppUser;

    private GreenMail greenMail;

    @BeforeEach
    public void beforeEach() {
        reset();

        greenMail = new GreenMailSupplier().get();
        greenMail.start();

        securityAppUser = appUserRepository.save(AppUser.builder()
                .email(PROFILE_EMAIL)
                .name("Get")
                .surname("Profile")
                .password(passwordEncoder.encode("password"))
                .enabled(true)
                .build());
    }

    @AfterEach
    public void afterEach() {
        greenMail.stop();
        reset();
    }

    private void reset() {
        testDbHelper.clean();
    }

    @Component
    private static class TestListener {
        @EventListener
        public void handle(OnPasswordResetTokenEmailEvent event) {
        }
    }

    @Test
    public void shouldCreateToken() throws Exception {
        mockMvc.perform(post(API_PREFIX_V1 + "/token-create")
                .contentType(MediaType.APPLICATION_JSON)
                .param("email", PROFILE_EMAIL))
                .andExpect(status().isOk());

        // OnPasswordResetTokenEmailEvent is fired.
        verify(testListener).handle(any(OnPasswordResetTokenEmailEvent.class));
    }

    @Test
    public void shouldntCreateTokenIfAppUserNotFound() throws Exception {
        mockMvc.perform(post(API_PREFIX_V1 + "/token-create")
                .contentType(MediaType.APPLICATION_JSON)
                .param("email", WRONG_EMAIL))
                .andExpect(status().isOk());

        // OnPasswordResetTokenEmailEvent shouldn't be fired.
        verifyNoMoreInteractions(testListener);
        assertThat(passwordService.findByEmail(PROFILE_EMAIL), isEmpty());
        assertThat(passwordService.findByEmail(WRONG_EMAIL), isEmpty());
    }

    @Test
    public void shouldValidateToken() throws Exception {
        PasswordReset token = PasswordReset.builder()
                .token(UUID.randomUUID().toString())
                .appUser(securityAppUser)
                .expiryTimestamp(LocalDateTime.now().plus(EXPIRE_IN))
                .build();
        passwordResetRepository.save(token);
        assertThat(passwordService.findByEmail(PROFILE_EMAIL), isPresent());

        mockMvc.perform(get(API_PREFIX_V1 + "/token-validate")
                .param("token", token.getToken()))
                .andExpect(status().isOk());

        assertThat(passwordService.findByEmail(PROFILE_EMAIL), isPresent());
    }

    @Test
    public void shouldntValidateTokenNotFound() throws Exception {
        mockMvc.perform(get(API_PREFIX_V1 + "/token-validate")
                .param("token", "notFoundToken"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void shouldntValidateTokenTokenExpired() throws Exception {
        PasswordReset token = PasswordReset.builder()
                .token(UUID.randomUUID().toString())
                .appUser(securityAppUser)
                .expiryTimestamp(LocalDateTime.now().minus(EXPIRE_IN))
                .build();
        passwordResetRepository.save(token);

        // status code for expired token should be 401
        mockMvc.perform(get(API_PREFIX_V1 + "/token-validate")
                .param("token", token.getToken()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void shouldResetPassword() throws Exception {
        PasswordReset token = PasswordReset.builder()
                .token(UUID.randomUUID().toString())
                .appUser(securityAppUser)
                .expiryTimestamp(LocalDateTime.now().plus(EXPIRE_IN))
                .build();
        passwordResetRepository.save(token);
        assertThat(passwordService.findByEmail(PROFILE_EMAIL), isPresent());

        String newPassword = "password2";
        String oldPassword = "password";
        PasswordResetRequest passwordReset = PasswordResetRequest.builder()
                .password(newPassword)
                .token(token.getToken())
                .build();

        mockMvc.perform(post(API_PREFIX_V1 + "/password-reset")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(passwordReset)))
                .andExpect(status().isOk());

        assertThat(passwordEncoder.matches(newPassword, appUserRepository.findByEmail(PROFILE_EMAIL).get().getPassword()), is(true));
        assertThat(passwordEncoder.matches(oldPassword, appUserRepository.findByEmail(PROFILE_EMAIL).get().getPassword()), is(false));
    }

    @Test
    public void shouldntResetPasswordTokenNotFound() throws Exception {
        PasswordReset token = PasswordReset.builder()
                .token(UUID.randomUUID().toString())
                .appUser(securityAppUser)
                .expiryTimestamp(LocalDateTime.now().plus(EXPIRE_IN))
                .build();
        passwordResetRepository.save(token);
        assertThat(passwordService.findByEmail(PROFILE_EMAIL), isPresent());

        String newPassword = "password2";
        PasswordResetRequest passwordReset = PasswordResetRequest.builder()
                .password(newPassword)
                .token("tokenNotFound")
                .build();

        mockMvc.perform(post(API_PREFIX_V1 + "/password-reset")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(passwordReset)))
                .andExpect(status().isNotFound());

        assertThat(passwordService.findByEmail(PROFILE_EMAIL), isPresent());
    }

    @Test
    public void shouldntResetPasswordTokenExpired() throws Exception {
        PasswordReset token = PasswordReset.builder()
                .token(UUID.randomUUID().toString())
                .appUser(securityAppUser)
                .expiryTimestamp(LocalDateTime.now().minus(EXPIRE_IN))
                .build();
        passwordResetRepository.save(token);
        assertThat(passwordService.findByEmail(PROFILE_EMAIL), isPresent());

        String newPassword = "password2";
        PasswordResetRequest passwordReset = PasswordResetRequest.builder()
                .password(newPassword)
                .token(token.getToken())
                .build();

        // status code for expired token should be 401
        mockMvc.perform(post(API_PREFIX_V1 + "/password-reset")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(passwordReset)))
                .andExpect(status().isUnauthorized());

        assertThat(passwordService.findByEmail(PROFILE_EMAIL), isPresent());
    }

    @Test
    public void shouldChangePassword() throws Exception {
        String newPassword = "password2";
        String oldPassword = "password";
        PasswordChangeRequest changeRequest = PasswordChangeRequest.builder()
                .newPassword(newPassword)
                .oldPassword(oldPassword)
                .build();
        assertThat(passwordEncoder.matches(oldPassword, appUserRepository.findByEmail(PROFILE_EMAIL).get().getPassword()), is(true));

        mockMvc.perform(post(API_PREFIX_V1 + "/password-change")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(changeRequest))
                .with(jwtBearerToken(securityAppUser, objectMapper)))
                .andExpect(status().isOk());

        assertThat(passwordEncoder.matches(newPassword, appUserRepository.findByEmail(PROFILE_EMAIL).get().getPassword()), is(true));
    }

    @Test
    public void shouldntChangePasswordBadRequest() throws Exception {
        String newPassword = "password2";
        String oldPassword = "password";
        PasswordChangeRequest changeRequest = PasswordChangeRequest.builder()
                .newPassword(newPassword)
                .oldPassword(newPassword)
                .build();
        assertThat(passwordEncoder.matches(oldPassword, appUserRepository.findByEmail(PROFILE_EMAIL).get().getPassword()), is(true));

        mockMvc.perform(post(API_PREFIX_V1 + "/password-change")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(changeRequest))
                .with(jwtBearerToken(securityAppUser, objectMapper)))
                .andExpect(status().isBadRequest());

        assertThat(passwordEncoder.matches(newPassword, appUserRepository.findByEmail(PROFILE_EMAIL).get().getPassword()), is(false));
        assertThat(passwordEncoder.matches(oldPassword, appUserRepository.findByEmail(PROFILE_EMAIL).get().getPassword()), is(true));
    }
}
