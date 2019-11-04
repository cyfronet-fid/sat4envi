package pl.cyfronet.s4e.data.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import pl.cyfronet.s4e.bean.Place;

public interface PlaceRepository extends CrudRepository<Place, Long> {
    Page<Place> findAllByNameIsStartingWithIgnoreCase(String name, Pageable pageable);
}
