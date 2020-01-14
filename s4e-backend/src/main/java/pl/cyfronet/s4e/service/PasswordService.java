package pl.cyfronet.s4e.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.cyfronet.s4e.bean.AppUser;
import pl.cyfronet.s4e.bean.PasswordReset;
import pl.cyfronet.s4e.controller.request.PasswordResetRequest;
import pl.cyfronet.s4e.data.repository.PasswordResetRepository;
import pl.cyfronet.s4e.ex.BadRequestException;
import pl.cyfronet.s4e.ex.NotFoundException;
import pl.cyfronet.s4e.ex.PasswordResetTokenExpiredException;
import pl.cyfronet.s4e.security.AppUserDetails;

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
        PasswordReset token = PasswordReset.builder()
                .token(UUID.randomUUID().toString())
                .appUser(appUserService.findByEmail(email)
                        .orElseThrow(() -> new NotFoundException("User not found for email: '" + email + "'")))
                .expiryTimestamp(LocalDateTime.now().plus(EXPIRE_IN))
                .build();
        return passwordResetRepository.save(token);
    }

    public void validate(String token) throws NotFoundException, PasswordResetTokenExpiredException {
        val resetPasswordToken = findByToken(token)
                .orElseThrow(() -> new NotFoundException("Provided token '" + token + "' not found"));

        if (resetPasswordToken.getExpiryTimestamp().isBefore(LocalDateTime.now())) {
            throw new PasswordResetTokenExpiredException("Provided token '" + token + "' expired");
        }
    }

    @Transactional(rollbackFor = {NotFoundException.class, BadRequestException.class})
    public void changePassword(PasswordResetRequest passwordReset) throws NotFoundException, BadRequestException {
        val appUser = appUserService.findByEmail(
                ((AppUserDetails) SecurityContextHolder.getContext().getAuthentication().getDetails()).getEmail())
                .orElseThrow(() -> new NotFoundException());
        if (appUser.getPassword().equals(passwordEncoder.encode(passwordReset.getOldPassword()))) {
            appUser.setPassword(passwordEncoder.encode(passwordReset.getNewPassword()));
            appUserService.update(appUser);
        } else {
            throw new BadRequestException("Provided passwords were incorrect");
        }
    }

    @Transactional(rollbackFor = NotFoundException.class)
    public void resetPassword(PasswordResetRequest passwordReset, String token) throws NotFoundException {
        val resetPasswordToken = findByToken(token)
                .orElseThrow(() -> new NotFoundException("Provided token '" + token + "' not found"));
        AppUser appUser = resetPasswordToken.getAppUser();
        appUser.setPassword(passwordEncoder.encode(passwordReset.getNewPassword()));
        appUserService.update(appUser);
        delete(resetPasswordToken.getId());
    }

    public Optional<PasswordReset> findByToken(String token) {
        return passwordResetRepository.findByToken(token);
    }

    @Transactional
    public void delete(Long id) {
        passwordResetRepository.deleteById(id);
    }
}
