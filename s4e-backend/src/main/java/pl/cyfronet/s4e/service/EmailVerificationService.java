package pl.cyfronet.s4e.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.cyfronet.s4e.bean.AppUser;
import pl.cyfronet.s4e.bean.EmailVerification;
import pl.cyfronet.s4e.data.repository.EmailVerificationRepository;

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

    public Optional<EmailVerification> findByAppUserId(Long appUserId) {
        return emailVerificationRepository.findByAppUserId(appUserId);
    }

    public Optional<EmailVerification> findByToken(String token) {
        return emailVerificationRepository.findByToken(token);
    }

    public EmailVerification create(AppUser appUser) {
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

    public void delete(Long id) {
        emailVerificationRepository.deleteById(id);
    }
}
