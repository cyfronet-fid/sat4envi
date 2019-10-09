package pl.cyfronet.s4e.data.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import pl.cyfronet.s4e.bean.Institution;

import java.util.Optional;

public interface InstitutionRepository extends CrudRepository<Institution, Long> {
    Page<Institution> findAll(Pageable pageable);

    void deleteInstitutionBySlug(String slug);

    Optional<Institution> findBySlug(String slug);

    @Modifying
    @Query("delete from Institution")
    void deleteAll();
}
