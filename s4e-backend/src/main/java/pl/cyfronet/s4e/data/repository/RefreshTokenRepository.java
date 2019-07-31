package pl.cyfronet.s4e.data.repository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.CrudRepository;
import pl.cyfronet.s4e.bean.RefreshToken;

import java.util.Optional;

public interface RefreshTokenRepository extends CrudRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByJti(String jti);
    @Modifying
    void deleteByJti(String jti);
}
