package pl.cyfronet.s4e.data.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;
import pl.cyfronet.s4e.bean.Institution;

import java.util.Optional;

@Transactional(readOnly = true)
public interface InstitutionRepository extends CrudRepository<Institution, Long> {
    <T> Page<T> findAllBy(Class<T> projection, Pageable pageable);

    <T> Optional<T> findBySlug(String slug, Class<T> projection);

    @Transactional
    @Modifying
    void deleteInstitutionBySlug(String slug);

    @Transactional
    @Modifying
    @Query("delete from Institution")
    void deleteAll();
}
