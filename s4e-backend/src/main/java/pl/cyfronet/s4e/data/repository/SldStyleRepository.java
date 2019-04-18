package pl.cyfronet.s4e.data.repository;

import org.springframework.data.repository.CrudRepository;
import pl.cyfronet.s4e.bean.SldStyle;

import java.util.Optional;

public interface SldStyleRepository extends CrudRepository<SldStyle, Long> {
    Optional<SldStyle> findByName(String name);
}
