package pl.cyfronet.s4e.data.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;
import pl.cyfronet.s4e.bean.Scene;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Transactional(readOnly = true)
public interface SceneRepository extends CrudRepository<Scene, Long> {
    <T> Optional<T> findById(Long id, Class<T> projection);

    List<Scene> findByProductId(Long productId);
    List<Scene> findAllByProductIdAndTimestampGreaterThanEqualAndTimestampLessThanOrderByTimestampAsc(
            Long productId, LocalDateTime start, LocalDateTime end
    );
    int countAllByProductIdAndTimestampGreaterThanEqualAndTimestampLessThan(
            Long productId, LocalDateTime start, LocalDateTime end
    );
}
