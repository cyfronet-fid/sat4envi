package pl.cyfronet.s4e.data.repository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;
import pl.cyfronet.s4e.bean.ProductCategory;

public interface ProductCategoryRepository extends CrudRepository<ProductCategory, Long> {
    public static final String DEFAULT_CATEGORY_LABEL = "Default";

    @Transactional
    @Modifying
    @Query("DELETE FROM ProductCategory WHERE label != :label")
    void deleteAllByLabelNot(String label);
}
