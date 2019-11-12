package pl.cyfronet.s4e.data.repository;

import org.springframework.data.repository.CrudRepository;
import pl.cyfronet.s4e.bean.Product;

import java.time.LocalDateTime;
import java.util.List;

public interface ProductRepository extends CrudRepository<Product, Long> {
    List<Product> findByProductTypeId(Long productTypeId);
    List<Product> findAllByProductTypeIdAndTimestampGreaterThanEqualAndTimestampLessThanOrderByTimestampAsc(
            Long productTypeId, LocalDateTime start, LocalDateTime end
    );
}
