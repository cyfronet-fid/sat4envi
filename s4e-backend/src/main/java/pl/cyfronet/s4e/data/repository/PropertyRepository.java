package pl.cyfronet.s4e.data.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;
import pl.cyfronet.s4e.bean.Property;

import java.util.List;
import java.util.Optional;

@Transactional(readOnly = true)
public interface PropertyRepository extends CrudRepository<Property, String> {
    Optional<Property> findByName(String name);

    <T> Optional<T> findByName(String name, Class<T> projection);

    <T> List<T> findAllBy(Class<T> projection);

    @Transactional
    void deleteByName(String name);
}
