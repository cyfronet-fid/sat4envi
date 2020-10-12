package pl.cyfronet.s4e.data.repository;

import org.springframework.data.domain.Sort;
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
    @EntityGraph(attributePaths = {"sceneSchema", "metadataSchema", "productCategory"})
    <T> List<T> findAllFetchSchemasAndCategory(Class<T> projection);

    @Query("SELECT p FROM Product p")
    @EntityGraph(attributePaths = {"productCategory"})
    <T> List<T> findAllFetchProductCategory(Sort sort, Class<T> projection);

    <T> Optional<T> findById(Long id, Class<T> projection);

    @Query("SELECT p FROM Product p WHERE p.id = :id")
    @EntityGraph(attributePaths = {"productCategory"})
    <T> Optional<T> findByIdFetchCategory(Long id, Class<T> projection);

    @Query("SELECT p FROM Product p WHERE p.id = :id")
    @EntityGraph(attributePaths = {"sceneSchema", "metadataSchema", "productCategory"})
    <T> Optional<T> findByIdFetchSchemasAndCategory(Long id, Class<T> projection);

    Optional<Product> findByNameContainingIgnoreCase(String name);

    @EntityGraph(attributePaths = {"sceneSchema", "metadataSchema", "productCategory"})
    <T> Optional<T> findByName(String name, Class<T> projection);

    boolean existsBySceneSchemaId(Long id);

    boolean existsByMetadataSchemaId(Long id);

    @Query("SELECT CASE WHEN COUNT(p)> 0 THEN 'true' ELSE 'false' END " +
            "FROM Product p " +
            "JOIN p.favourites f " +
            "WHERE p.id = :productId AND f.email = :email")
    boolean isFavouriteByEmailAndProductId(String email, Long productId);
}
