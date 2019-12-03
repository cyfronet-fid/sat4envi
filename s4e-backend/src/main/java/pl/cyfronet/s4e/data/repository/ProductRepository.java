package pl.cyfronet.s4e.data.repository;

import org.springframework.data.repository.CrudRepository;
import pl.cyfronet.s4e.bean.Product;

import java.util.Optional;

public interface ProductRepository extends CrudRepository<Product, Long> {
    Optional<Product> findByNameContainingIgnoreCase(String name);
}
