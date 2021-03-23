/*
 * Copyright 2020 ACC Cyfronet AGH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package pl.cyfronet.s4e.service;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import pl.cyfronet.s4e.BasicTest;
import pl.cyfronet.s4e.TestDbHelper;
import pl.cyfronet.s4e.bean.AppUser;
import pl.cyfronet.s4e.bean.PasswordReset;
import pl.cyfronet.s4e.controller.request.PasswordChangeRequest;
import pl.cyfronet.s4e.controller.request.PasswordResetRequest;
import pl.cyfronet.s4e.data.repository.AppUserRepository;
import pl.cyfronet.s4e.data.repository.PasswordResetRepository;
import pl.cyfronet.s4e.ex.BadRequestException;
import pl.cyfronet.s4e.ex.NotFoundException;
import pl.cyfronet.s4e.ex.PasswordResetTokenExpiredException;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAmount;
import java.util.UUID;

import static com.github.npathai.hamcrestopt.OptionalMatchers.isEmpty;
import static com.github.npathai.hamcrestopt.OptionalMatchers.isPresent;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

@BasicTest
@Slf4j
public class PasswordServiceTest {
    private static final String PROFILE_EMAIL = "get@profile.com";
    private static final TemporalAmount EXPIRE_IN = Duration.ofDays(1);

    @Autowired
    private TestDbHelper testDbHelper;

    @Autowired
    private PasswordResetRepository passwordResetRepository;

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private PasswordService passwordService;

    @Autowired
    private AppUserService appUserService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private AppUser appUser;

    @BeforeEach
    public void setUp() {
        reset();
        appUser = appUserRepository.save(AppUser.builder()
                .email(PROFILE_EMAIL)
                .name("Get")
                .surname("Profile")
                .password(passwordEncoder.encode("password"))
                .enabled(false)
                .build());
    }

    @AfterEach
    void tearDown() {
        reset();
    }

    private void reset() {
        testDbHelper.clean();
    }

    @Test
    public void shouldValidateThatTokenIsNotValid() {
        PasswordReset token = PasswordReset.builder()
                .token(UUID.randomUUID().toString())
                .appUser(appUser)
                .expiryTimestamp(LocalDateTime.now().minus(EXPIRE_IN))
                .build();
        passwordResetRepository.save(token);

        assertThrows(PasswordResetTokenExpiredException.class, () -> passwordService.validate(token.getToken()));
        assertThrows(NotFoundException.class, () -> passwordService.validate("notFoundToken"));
    }

    @Test
    public void shouldValidateThatTokenIsValid() throws Exception{
        PasswordReset token = PasswordReset.builder()
                .token(UUID.randomUUID().toString())
                .appUser(appUser)
                .expiryTimestamp(LocalDateTime.now().plus(EXPIRE_IN))
                .build();
        passwordResetRepository.save(token);
        passwordService.validate(token.getToken());
    }

    @Test
    public void shouldFindByEmail() {
        PasswordReset token = PasswordReset.builder()
                .token(UUID.randomUUID().toString())
                .appUser(appUser)
                .expiryTimestamp(LocalDateTime.now().minus(EXPIRE_IN))
                .build();
        passwordResetRepository.save(token);

        assertThat(passwordService.findByEmail(PROFILE_EMAIL), isPresent());
    }

    @Test
    public void shouldFindByToken() {
        PasswordReset token = PasswordReset.builder()
                .token(UUID.randomUUID().toString())
                .appUser(appUser)
                .expiryTimestamp(LocalDateTime.now().minus(EXPIRE_IN))
                .build();
        passwordResetRepository.save(token);

        assertThat(passwordService.findByToken(token.getToken()), isPresent());
    }

    @Test
    public void shouldResetPassword() throws Exception {
        assertThat(appUserService.findByEmail(PROFILE_EMAIL).get().isEnabled(), is(false));
        PasswordReset token = PasswordReset.builder()
                .token(UUID.randomUUID().toString())
                .appUser(appUserService.findByEmail(PROFILE_EMAIL).get())
                .expiryTimestamp(LocalDateTime.now().plus(EXPIRE_IN))
                .build();
        passwordResetRepository.save(token);
        String password = "password2";
        PasswordResetRequest passwordReset = PasswordResetRequest.builder()
                .password(password)
                .token(token.getToken())
                .build();
        passwordService.resetPassword(passwordReset);

        assertThat(appUserService.findByEmail(PROFILE_EMAIL).get().isEnabled(), is(true));
        assertThat(passwordService.findByEmail(PROFILE_EMAIL), isEmpty());
        assertThat(passwordEncoder.matches(password, appUserRepository.findByEmail(PROFILE_EMAIL).get().getPassword()), is(true));
    }

    @Test
    public void shouldntResetPasswordNotFound() {
        PasswordReset token = PasswordReset.builder()
                .token(UUID.randomUUID().toString())
                .appUser(appUserService.findByEmail(PROFILE_EMAIL).get())
                .expiryTimestamp(LocalDateTime.now().plus(EXPIRE_IN))
                .build();
        passwordResetRepository.save(token);
        String newPassword = "password2";
        PasswordResetRequest passwordReset = PasswordResetRequest.builder()
                .password(newPassword)
                .token("notFoundToken")
                .build();

        assertThrows(NotFoundException.class, () -> passwordService.resetPassword(passwordReset));
        assertThat(passwordService.findByEmail(PROFILE_EMAIL), isPresent());
    }

    @Test
    public void shouldntResetPasswordTokenExpired() {
        PasswordReset token = PasswordReset.builder()
                .token(UUID.randomUUID().toString())
                .appUser(appUserService.findByEmail(PROFILE_EMAIL).get())
                .expiryTimestamp(LocalDateTime.now().minus(EXPIRE_IN))
                .build();
        passwordResetRepository.save(token);
        String newPassword = "password2";
        PasswordResetRequest passwordReset = PasswordResetRequest.builder()
                .password(newPassword)
                .token(token.getToken())
                .build();

        assertThrows(PasswordResetTokenExpiredException.class, () -> passwordService.resetPassword(passwordReset));
        assertThat(passwordService.findByEmail(PROFILE_EMAIL), isPresent());
    }

    @Test
    public void shouldChangePassword() throws Exception {
        String newPassword = "password2";
        String oldPassword = "password";
        PasswordChangeRequest passwordReset = PasswordChangeRequest.builder()
                .newPassword(newPassword)
                .oldPassword(oldPassword)
                .build();

        assertThat(passwordEncoder.matches(oldPassword, appUserRepository.findByEmail(PROFILE_EMAIL).get().getPassword()), is(true));
        passwordService.changePassword(passwordReset, PROFILE_EMAIL);
        assertThat(passwordEncoder.matches(newPassword, appUserRepository.findByEmail(PROFILE_EMAIL).get().getPassword()), is(true));
    }

    @Test
    public void shouldntChangePasswordBadRequest() {
        String newPassword = "password2";
        String oldPassword = "password";
        PasswordChangeRequest changeRequest = PasswordChangeRequest.builder()
                .newPassword(newPassword)
                .oldPassword(newPassword)
                .build();

        assertThat(passwordEncoder.matches(oldPassword, appUserRepository.findByEmail(PROFILE_EMAIL).get().getPassword()), is(true));
        assertThrows(BadRequestException.class, () -> passwordService.changePassword(changeRequest, PROFILE_EMAIL));
        assertThat(passwordEncoder.matches(newPassword, appUserRepository.findByEmail(PROFILE_EMAIL).get().getPassword()), is(false));
        assertThat(passwordEncoder.matches(oldPassword, appUserRepository.findByEmail(PROFILE_EMAIL).get().getPassword()), is(true));
    }

    @Test
    public void shouldntChangePasswordNotFound() {
        String newPassword = "password2";
        String oldPassword = "password";
        PasswordChangeRequest changeRequest = PasswordChangeRequest.builder()
                .newPassword(newPassword)
                .oldPassword(newPassword)
                .build();

        assertThat(passwordEncoder.matches(oldPassword, appUserRepository.findByEmail(PROFILE_EMAIL).get().getPassword()), is(true));
        assertThrows(NotFoundException.class, () -> passwordService.changePassword(changeRequest, "email@email.pl"));
        assertThat(passwordEncoder.matches(newPassword, appUserRepository.findByEmail(PROFILE_EMAIL).get().getPassword()), is(false));
        assertThat(passwordEncoder.matches(oldPassword, appUserRepository.findByEmail(PROFILE_EMAIL).get().getPassword()), is(true));
    }

    @Nested
    class CreatePasswordResetTokenForUser {
        @Autowired
        private PasswordResetRepository passwordResetRepository;

        @Test
        public void shouldWork() throws Exception {
            assertThat(passwordService.findByEmail(PROFILE_EMAIL), isEmpty());
            passwordService.createPasswordResetTokenForUser(PROFILE_EMAIL);
            assertThat(passwordService.findByEmail(PROFILE_EMAIL), isPresent());
        }

        @Test
        public void shouldRecreateToken() throws Exception {
            final LocalDateTime originalExpiryTimestamp = LocalDateTime.now();
            Long originalResetId = passwordResetRepository.save(PasswordReset.builder()
                    .appUser(appUser)
                    .token("someValue")
                    .expiryTimestamp(originalExpiryTimestamp)
                    .build()).getId();
            assertThat(passwordResetRepository.findByAppUserEmail(PROFILE_EMAIL), isPresent());

            passwordService.createPasswordResetTokenForUser(PROFILE_EMAIL);

            assertThat(passwordResetRepository.existsById(originalResetId), is(true));
            assertThat(passwordResetRepository.findById(originalResetId).get(), allOf(
                    hasProperty("appUser", is(equalTo(appUser))),
                    hasProperty("token", not(equalTo("someValue"))),
                    hasProperty("expiryTimestamp", is(greaterThan(originalExpiryTimestamp)))
            ));
        }

        @Test
        public void shouldVerifyUserExists() {
            assertThat(passwordService.findByEmail("doesnt@exist.pl"), isEmpty());
            assertThrows(NotFoundException.class, () -> passwordService.createPasswordResetTokenForUser("doesnt@exist.pl"));
            assertThat(passwordService.findByEmail("doesnt@exist.pl"), isEmpty());
        }
    }
}
