package pl.cyfronet.s4e.data.repository;

import org.springframework.data.repository.CrudRepository;
import pl.cyfronet.s4e.bean.EmailVerification;

import java.util.Optional;

public interface EmailVerificationRepository extends CrudRepository<EmailVerification, Long> {
    Optional<EmailVerification> findByToken(String token);
    Optional<EmailVerification> findByAppUserId(Long appUserId);
}
