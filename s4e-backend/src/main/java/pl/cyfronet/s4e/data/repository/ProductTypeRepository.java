package pl.cyfronet.s4e.data.repository;

import org.springframework.data.repository.CrudRepository;
import pl.cyfronet.s4e.bean.ProductType;

public interface ProductTypeRepository extends CrudRepository<ProductType, Long> {
}
