package pl.cyfronet.s4e.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.cyfronet.s4e.bean.AppUser;
import pl.cyfronet.s4e.bean.EmailVerification;
import pl.cyfronet.s4e.data.repository.AppUserRepository;
import pl.cyfronet.s4e.data.repository.EmailVerificationRepository;
import pl.cyfronet.s4e.ex.NotFoundException;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAmount;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EmailVerificationService {
    private static final TemporalAmount EXPIRE_IN = Duration.ofDays(1);

    private final EmailVerificationRepository emailVerificationRepository;
    private final AppUserRepository appUserRepository;

    public Optional<EmailVerification> findByAppUserEmail(String email) {
        return emailVerificationRepository.findByAppUserEmail(email);
    }

    public Optional<EmailVerification> findByToken(String token) {
        return emailVerificationRepository.findByToken(token);
    }

    @Transactional(rollbackFor = NotFoundException.class)
    public EmailVerification create(String email) throws NotFoundException {
        AppUser appUser = appUserRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found for email: '" + email + "'"));
        if (appUser.isEnabled()) {
            throw new IllegalStateException("Cannot create ValidationToken for an enabled AppUser");
        }
        String token = UUID.randomUUID().toString();
        return emailVerificationRepository.save(EmailVerification.builder()
                .appUser(appUser)
                .token(token)
                .expiryTimestamp(LocalDateTime.now().plus(EXPIRE_IN))
                .build());
    }

    @Transactional
    public void delete(Long id) {
        emailVerificationRepository.deleteById(id);
    }
}
