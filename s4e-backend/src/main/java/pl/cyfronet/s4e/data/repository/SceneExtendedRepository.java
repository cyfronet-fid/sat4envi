package pl.cyfronet.s4e.data.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;
import pl.cyfronet.s4e.bean.SceneExtended;

import java.util.Optional;

@Transactional(readOnly = true)
public interface SceneExtendedRepository extends JpaRepository<SceneExtended, Long> {
    <T> Optional<T> findById(Long id, Class<T> projection);
}
