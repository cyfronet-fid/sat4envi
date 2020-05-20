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

    List<Scene> findAllByProductId(Long productId);

    List<Scene> findAllByProductIdAndTimestampGreaterThanEqualAndTimestampLessThanOrderByTimestampAsc(
            Long productId, LocalDateTime start, LocalDateTime end
    );

    int countAllByProductIdAndTimestampGreaterThanEqualAndTimestampLessThan(
            Long productId, LocalDateTime start, LocalDateTime end
    );
}
