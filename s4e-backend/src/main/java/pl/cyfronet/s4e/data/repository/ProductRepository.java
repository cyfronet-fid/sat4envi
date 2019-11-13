package pl.cyfronet.s4e.data.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import pl.cyfronet.s4e.bean.Product;

import java.sql.Date;
import java.time.LocalDateTime;
import java.util.List;

public interface ProductRepository extends CrudRepository<Product, Long> {
    List<Product> findByProductTypeId(Long productTypeId);
    List<Product> findAllByProductTypeIdAndTimestampGreaterThanEqualAndTimestampLessThanOrderByTimestampAsc(
            Long productTypeId, LocalDateTime start, LocalDateTime end
    );

    // I wanted the method to return List<LocalDate>, but spring-data seems to have a problem with conversions
    // for native queries. At least the solutions from
    // https://stackoverflow.com/questions/46920073/java-time-localdate-not-supported-in-native-queries-by-latest-spring-data-hibern
    // (the EDIT and the second answer) didn't work.
    @Query(value =
            "SELECT DISTINCT DATE(p.timestamp) " +
            "FROM Product AS p " +
            "WHERE p.product_type_id = :productTypeId AND p.timestamp >= :start AND p.timestamp < :end",
            nativeQuery = true)
    List<Date> findDatesWithData(Long productTypeId, LocalDateTime start, LocalDateTime end);
}
