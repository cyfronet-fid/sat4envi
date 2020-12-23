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

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.cyfronet.s4e.bean.AppUser;
import pl.cyfronet.s4e.bean.PasswordReset;
import pl.cyfronet.s4e.controller.request.PasswordChangeRequest;
import pl.cyfronet.s4e.controller.request.PasswordResetRequest;
import pl.cyfronet.s4e.data.repository.PasswordResetRepository;
import pl.cyfronet.s4e.ex.BadRequestException;
import pl.cyfronet.s4e.ex.NotFoundException;
import pl.cyfronet.s4e.ex.PasswordResetTokenExpiredException;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAmount;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordService {
    private static final TemporalAmount EXPIRE_IN = Duration.ofDays(1);
    private final AppUserService appUserService;
    private final PasswordEncoder passwordEncoder;
    private final PasswordResetRepository passwordResetRepository;

    @Transactional(rollbackFor = NotFoundException.class)
    public PasswordReset createPasswordResetTokenForUser(String email) throws NotFoundException {
        val appUser = appUserService.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found for email: '" + email + "'"));
        PasswordReset passwordReset = passwordResetRepository.findByAppUserEmail(email)
                .orElseGet(() -> PasswordReset.builder().appUser(appUser).build());
        passwordReset.setToken(UUID.randomUUID().toString());
        passwordReset.setExpiryTimestamp(LocalDateTime.now().plus(EXPIRE_IN));
        if (passwordReset.getId() == null) {
            return passwordResetRepository.save(passwordReset);
        }
        return passwordReset;
    }

    public void validate(String token) throws NotFoundException, PasswordResetTokenExpiredException {
        val resetPasswordToken = findByToken(token)
                .orElseThrow(() -> new NotFoundException("Provided token '" + token + "' not found"));

        if (resetPasswordToken.getExpiryTimestamp().isBefore(LocalDateTime.now())) {
            throw new PasswordResetTokenExpiredException("Provided token '" + token + "' expired");
        }
    }

    @Transactional(rollbackFor = {NotFoundException.class, BadRequestException.class})
    public void changePassword(PasswordChangeRequest passwordReset, String email) throws NotFoundException, BadRequestException {
        val appUser = appUserService.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found"));
        if (passwordEncoder.matches(passwordReset.getOldPassword(), appUser.getPassword())) {
            appUser.setPassword(passwordEncoder.encode(passwordReset.getNewPassword()));
        } else {
            throw new BadRequestException("Provided passwords were incorrect");
        }
    }

    @Transactional(rollbackFor = {NotFoundException.class, PasswordResetTokenExpiredException.class})
    public void resetPassword(PasswordResetRequest passwordReset) throws NotFoundException, PasswordResetTokenExpiredException {
        val resetPasswordToken = findByToken(passwordReset.getToken())
                .orElseThrow(() -> new NotFoundException("Provided token '" + passwordReset.getToken() + "' not found"));
        if (resetPasswordToken.getExpiryTimestamp().isBefore(LocalDateTime.now())) {
            throw new PasswordResetTokenExpiredException("Provided token '" + passwordReset.getToken() + "' expired");
        }
        AppUser appUser = resetPasswordToken.getAppUser();
        appUser.setPassword(passwordEncoder.encode(passwordReset.getPassword()));
        passwordResetRepository.deleteById(resetPasswordToken.getId());
    }

    public Optional<PasswordReset> findByToken(String token) {
        return passwordResetRepository.findByToken(token);
    }

    public Optional<PasswordReset> findByEmail(String email) {
        return passwordResetRepository.findByAppUserEmail(email);
    }
}
