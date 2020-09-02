package pl.cyfronet.s4e.data.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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

    <T> Optional<T> findFirstByProductId(Long productId, Sort sort, Class<T> projection);

    List<Scene> findAllByProductId(Long productId);

    <T> List<T> findAllByProductIdAndTimestampGreaterThanEqualAndTimestampLessThanOrderByTimestampAsc(
            Long productId, LocalDateTime start, LocalDateTime end, Class<T> projection
    );

    <T> Page<T> findAllBy(Pageable pageable, Class<T> projection);

    <T> Page<T> findAllByProductId(Long productId, Pageable pageable, Class<T> projection);

    int countAllByProductIdAndTimestampGreaterThanEqualAndTimestampLessThan(
            Long productId, LocalDateTime start, LocalDateTime end
    );

    @Transactional
    void deleteAllByProductId(Long productId);
}
