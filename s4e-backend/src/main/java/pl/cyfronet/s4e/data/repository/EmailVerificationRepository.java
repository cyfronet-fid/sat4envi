package pl.cyfronet.s4e.data.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;
import pl.cyfronet.s4e.bean.EmailVerification;

import java.util.Optional;

@Transactional(readOnly = true)
public interface EmailVerificationRepository extends CrudRepository<EmailVerification, Long> {
    Optional<EmailVerification> findByToken(String token);
    Optional<EmailVerification> findByAppUserId(Long appUserId);
    Optional<EmailVerification> findByAppUserEmail(String email);
}
