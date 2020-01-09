package pl.cyfronet.s4e.data.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;
import pl.cyfronet.s4e.bean.Scene;

import java.time.LocalDateTime;
import java.util.List;

@Transactional(readOnly = true)
public interface SceneRepository extends CrudRepository<Scene, Long> {
    List<Scene> findByProductId(Long productId);
    List<Scene> findAllByProductIdAndTimestampGreaterThanEqualAndTimestampLessThanOrderByTimestampAsc(
            Long productId, LocalDateTime start, LocalDateTime end
    );
    int countAllByProductIdAndTimestampGreaterThanEqualAndTimestampLessThan(
            Long productId, LocalDateTime start, LocalDateTime end
    );
}
