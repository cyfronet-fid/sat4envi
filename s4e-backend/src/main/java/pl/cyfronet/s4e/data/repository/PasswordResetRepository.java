package pl.cyfronet.s4e.data.repository;

import org.springframework.data.repository.CrudRepository;
import pl.cyfronet.s4e.bean.PasswordReset;

import java.util.Optional;

public interface PasswordResetRepository extends CrudRepository<PasswordReset, Long> {
    Optional<PasswordReset> findByToken(String token);
}
