package pl.cyfronet.s4e.data.repository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;
import pl.cyfronet.s4e.bean.AppRole;
import pl.cyfronet.s4e.bean.Institution;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Transactional(readOnly = true)
public interface InstitutionRepository extends CrudRepository<Institution, Long> {
    <T> Optional<T> findById(Long id, Class<T> projection);

    <T> List<T> findAllBy(Class<T> projection);

    <T> Optional<T> findBySlug(String slug, Class<T> projection);

    @Query(value = "SELECT i.* " +
            "FROM Institution i " +
            "LEFT JOIN user_role r ON r.institution_id = i.id " +
            "LEFT JOIN app_user u ON u.id = r.app_user_id " +
            "WHERE u.email = :email " +
            "AND r.role in (:roles)", nativeQuery = true)
    <T> List<T> findInstitutionByUserEmailAndRoles(String email, List<String> roles, Class<T> projection);

    @Query("SELECT u " +
            "FROM AppUser u " +
            "LEFT JOIN FETCH u.roles r " +
            "LEFT JOIN FETCH r.institution i " +
            "WHERE i.slug = :institutionSlug AND r.role = :role")
    <T> Set<T> findAllMembers(String institutionSlug, AppRole role, Class<T> projection);

    @Query("SELECT u.email " +
            "FROM UserRole r " +
            "JOIN r.user u " +
            "JOIN r.institution i " +
            "WHERE i.slug = :institutionSlug AND r.role = :role")
    Set<String> findAllMembersEmails(String institutionSlug, AppRole role);

    @Transactional
    @Modifying
    void deleteInstitutionBySlug(String slug);

    @Transactional
    @Modifying
    @Query("delete from Institution")
    void deleteAll();
}
