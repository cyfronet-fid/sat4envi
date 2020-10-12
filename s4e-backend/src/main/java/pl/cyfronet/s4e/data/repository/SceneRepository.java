package pl.cyfronet.s4e.data.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.transaction.annotation.Transactional;
import pl.cyfronet.s4e.bean.Scene;

import javax.persistence.QueryHint;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.hibernate.jpa.QueryHints.HINT_FETCH_SIZE;

@Transactional(readOnly = true)
public interface SceneRepository extends JpaRepository<Scene, Long>, SceneRepositoryExt {
    <T> Optional<T> findById(Long id, Class<T> projection);

    @Query("SELECT s FROM Scene s WHERE s.id = :id")
    @EntityGraph(attributePaths = "product")
    Optional<Scene> findByIdFetchProduct(Long id);

    Optional<Scene> findBySceneKey(String sceneKey);

    <T> Optional<T> findFirstByProductId(Long productId, Sort sort, Class<T> projection);

    List<Scene> findAllByProductId(Long productId);

    @QueryHints(@QueryHint(name = HINT_FETCH_SIZE, value = "4"))
    <T> Stream<T> streamAllByProductId(Long productId, Sort sort, Class<T> projection);

    @Query("SELECT s FROM Scene s WHERE s.product.id = :productId AND s.timestamp >= :start AND s.timestamp < :end")
    <T> List<T> findAllInTimestampRangeForProduct(
            Long productId, LocalDateTime start, LocalDateTime end, Sort sort, Class<T> projection
    );

    <T> Page<T> findAllBy(Pageable pageable, Class<T> projection);

    <T> Page<T> findAllByProductId(Long productId, Pageable pageable, Class<T> projection);

    int countAllByProductIdAndTimestampGreaterThanEqualAndTimestampLessThan(
            Long productId, LocalDateTime start, LocalDateTime end
    );

    @Transactional
    void deleteAllByProductId(Long productId);
}
