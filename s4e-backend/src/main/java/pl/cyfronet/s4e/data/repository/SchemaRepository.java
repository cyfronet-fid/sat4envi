package pl.cyfronet.s4e.data.repository;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;
import pl.cyfronet.s4e.bean.Schema;

import java.util.List;
import java.util.Optional;

@Transactional(readOnly = true)
public interface SchemaRepository extends CrudRepository<Schema, Long> {
    Optional<Schema> findByName(String name);

    <T> Optional<T> findByName(String name, Class<T> projection);

    <T> List<T> findAllBy(Class<T> projection, Sort sort);

    @Query("SELECT s.content FROM Schema s WHERE s.name = :name")
    String getContentByName(String name);

    boolean existsByName(String name);

    boolean existsByPreviousId(Long id);
}
