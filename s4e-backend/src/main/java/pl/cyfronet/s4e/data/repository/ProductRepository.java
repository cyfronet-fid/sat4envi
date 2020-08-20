package pl.cyfronet.s4e.data.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;
import pl.cyfronet.s4e.bean.Product;

import java.util.List;
import java.util.Optional;

@Transactional(readOnly = true)
public interface ProductRepository extends CrudRepository<Product, Long> {
    <T> List<T> findAllByOrderByIdAsc(Class<T> projection);

    @Query("SELECT p FROM Product p")
    @EntityGraph(attributePaths = {"sceneSchema", "metadataSchema"})
    <T> List<T> findAllFetchSchemas(Class<T> projection);

    <T> Optional<T> findById(Long id, Class<T> projection);

    @Query("SELECT p FROM Product p WHERE p.id = :id")
    @EntityGraph(attributePaths = {"sceneSchema", "metadataSchema"})
    <T> Optional<T> findByIdFetchSchemas(Long id, Class<T> projection);

    Optional<Product> findByNameContainingIgnoreCase(String name);

    @EntityGraph(attributePaths = {"sceneSchema", "metadataSchema"})
    <T> Optional<T> findByName(String name, Class<T> projection);

    boolean existsBySceneSchemaId(Long id);

    boolean existsByMetadataSchemaId(Long id);

    @Query("SELECT CASE WHEN COUNT(p)> 0 THEN 'true' ELSE 'false' END " +
            "FROM Product p " +
            "JOIN p.favourites f " +
            "WHERE p.id = :productId AND f.email = :email")
    boolean isFavouriteByEmailAndProductId(String email, Long productId);
}
