package pl.cyfronet.s4e.data.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import pl.cyfronet.s4e.bean.Scene;

import java.sql.Date;
import java.time.LocalDateTime;
import java.util.List;

public interface SceneRepository extends CrudRepository<Scene, Long> {
    List<Scene> findByProductTypeId(Long productTypeId);
    List<Scene> findAllByProductTypeIdAndTimestampGreaterThanEqualAndTimestampLessThanOrderByTimestampAsc(
            Long productTypeId, LocalDateTime start, LocalDateTime end
    );

    // I wanted the method to return List<LocalDate>, but spring-data seems to have a problem with conversions
    // for native queries. At least the solutions from
    // https://stackoverflow.com/questions/46920073/java-time-localdate-not-supported-in-native-queries-by-latest-spring-data-hibern
    // (the EDIT and the second answer) didn't work.
    @Query(value =
            "SELECT DISTINCT DATE(s.timestamp) " +
            "FROM Scene AS s " +
            "WHERE s.product_type_id = :productTypeId AND s.timestamp >= :start AND s.timestamp < :end",
            nativeQuery = true)
    List<Date> findDatesWithData(Long productTypeId, LocalDateTime start, LocalDateTime end);
}
