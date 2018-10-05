package pl.cyfronet.s4e.granules;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface GranuleRepository extends CrudRepository<Granule, Long> {
    List<Granule> findByProductId(Long productId);
}
