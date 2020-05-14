package pl.cyfronet.s4e.data.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;
import pl.cyfronet.s4e.bean.Institution;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Transactional(readOnly = true)
public interface InstitutionRepository extends CrudRepository<Institution, Long> {
    <T> Page<T> findAllBy(Class<T> projection, Pageable pageable);

    <T> Optional<T> findBySlug(String slug, Class<T> projection);

    @Query(value = "SELECT i.* " +
            "FROM Institution i " +
            "LEFT JOIN inst_group g ON g.institution_id = i.id " +
            "LEFT JOIN user_role r ON r.inst_group_id = g.id " +
            "LEFT JOIN app_user u ON u.id = r.app_user_id " +
            "WHERE u.email = :email " +
            "AND r.role in (:roles)", nativeQuery = true)
    <T> Set<T> findInstitutionByUserEmailAndRoles(String email, List<String> roles, Class<T> projection);

    @Transactional
    @Modifying
    void deleteInstitutionBySlug(String slug);

    @Transactional
    @Modifying
    @Query("delete from Institution")
    void deleteAll();
}
