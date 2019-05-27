package pl.cyfronet.s4e.data.repository;

import org.springframework.data.repository.CrudRepository;
import pl.cyfronet.s4e.bean.ProductType;

import java.util.Optional;

public interface ProductTypeRepository extends CrudRepository<ProductType, Long> {
    Optional<ProductType> findByNameContainingIgnoreCase(String name);
}
