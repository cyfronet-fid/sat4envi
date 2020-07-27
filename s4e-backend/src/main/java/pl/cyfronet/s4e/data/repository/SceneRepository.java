package pl.cyfronet.s4e.data.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;
import pl.cyfronet.s4e.bean.Scene;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Transactional(readOnly = true)
public interface SceneRepository extends JpaRepository<Scene, Long>, SceneRepositoryExt {
    <T> Optional<T> findById(Long id, Class<T> projection);

    Optional<Scene> findBySceneKey(String sceneKey);

    List<Scene> findAllByProductId(Long productId);

    <T> List<T> findAllByProductIdAndTimestampGreaterThanEqualAndTimestampLessThanOrderByTimestampAsc(
            Long productId, LocalDateTime start, LocalDateTime end, Class<T> projection
    );

    int countAllByProductIdAndTimestampGreaterThanEqualAndTimestampLessThan(
            Long productId, LocalDateTime start, LocalDateTime end
    );
}
