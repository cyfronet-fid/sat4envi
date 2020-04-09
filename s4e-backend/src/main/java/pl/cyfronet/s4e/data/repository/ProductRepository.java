package pl.cyfronet.s4e.data.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;
import pl.cyfronet.s4e.bean.Product;

import java.util.List;
import java.util.Optional;

@Transactional(readOnly = true)
public interface ProductRepository extends CrudRepository<Product, Long> {
    Optional<Product> findByNameContainingIgnoreCase(String name);

    <T> List<T> findAllBy(Class<T> projection);

    @Query("SELECT CASE WHEN COUNT(p)> 0 THEN 'true' ELSE 'false' END FROM Product p " +
            "JOIN p.favourites f " +
            "WHERE p.id = :productId AND f.email = :email")
    boolean isFavouriteByEmailAndProductId(String email, Long productId);

    <T> Optional<T> findById(Long id, Class<T> projection);

    boolean existsBySceneSchemaId(Long id);

    boolean existsByMetadataSchemaId(Long id);
}
