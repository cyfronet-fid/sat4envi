package pl.cyfronet.s4e.data.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;
import pl.cyfronet.s4e.bean.Place;
import pl.cyfronet.s4e.controller.response.PlaceResponse;

@Transactional(readOnly = true)
public interface PlaceRepository extends CrudRepository<Place, Long> {
    Page<PlaceResponse> findAllByNameIsStartingWithIgnoreCase(String name, Pageable pageable);
}
