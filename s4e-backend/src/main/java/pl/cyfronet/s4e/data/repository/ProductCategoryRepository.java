package pl.cyfronet.s4e.data.repository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;
import pl.cyfronet.s4e.bean.ProductCategory;

import java.util.Optional;

public interface ProductCategoryRepository extends CrudRepository<ProductCategory, Long> {
    String DEFAULT_CATEGORY_NAME = "other";

    <T> Optional<T> findByName(String name, Class<T> projection);

    @Transactional
    @Modifying
    @Query("DELETE FROM ProductCategory WHERE name != :name")
    void deleteAllByNameNot(String name);
}
