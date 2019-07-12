package pl.cyfronet.s4e.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.cyfronet.s4e.bean.AppUser;
import pl.cyfronet.s4e.bean.PasswordReset;
import pl.cyfronet.s4e.data.repository.PasswordResetRepository;

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
    private final PasswordResetRepository passwordResetRepository;

    public PasswordReset createPasswordResetTokenForUser(AppUser user) {
        PasswordReset token = PasswordReset.builder().token(UUID.randomUUID().toString()).appUser(user).expiryTimestamp(LocalDateTime.now().plus(EXPIRE_IN)).build();
        return passwordResetRepository.save(token);
    }

    public Optional<PasswordReset> findByToken(String token) {
        return passwordResetRepository.findByToken(token);
    }

    public void delete(Long id) {
        passwordResetRepository.deleteById(id);
    }
}
