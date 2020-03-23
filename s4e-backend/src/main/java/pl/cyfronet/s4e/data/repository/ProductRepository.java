package pl.cyfronet.s4e.data.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;
import pl.cyfronet.s4e.bean.Product;

import java.util.List;
import java.util.Optional;

@Transactional(readOnly = true)
public interface ProductRepository extends CrudRepository<Product, Long> {
    Optional<Product> findByNameContainingIgnoreCase(String name);

    <T> List<T> findAllBy(Class<T> projection);

    <T> Optional<T> findById(Long id, Class<T> projection);

    boolean existsBySceneSchemaId(Long id);

    boolean existsByMetadataSchemaId(Long id);
}
