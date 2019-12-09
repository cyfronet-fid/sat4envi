package pl.cyfronet.s4e.data.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;
import pl.cyfronet.s4e.bean.AppRole;
import pl.cyfronet.s4e.bean.AppUser;
import pl.cyfronet.s4e.bean.Group;

import java.util.Optional;
import java.util.Set;

@Transactional(readOnly = true)
public interface GroupRepository extends CrudRepository<Group, Long> {
    Page<Group> findAllByInstitution_Slug(String slug, Pageable pageable);

    Optional<Group> findByInstitution_SlugAndSlug(String institutionSlug, String slug);

    @Query(value = "SELECT g " +
            "FROM UserRole r " +
            "JOIN r.user u " +
            "JOIN r.group g " +
            "JOIN g.institution i " +
            "WHERE i.slug = :institutionSlug AND u.email = :email AND r.role = :role")
    Set<Group> findAllByInstitutionAndMemberEmail(String institutionSlug, String email, AppRole role);

    @Query("SELECT u " +
            "FROM AppUser u " +
            "LEFT JOIN FETCH u.roles r " +
            "LEFT JOIN FETCH r.group g " +
            "LEFT JOIN FETCH g.institution i " +
            "WHERE g.institution.slug = :institutionSlug AND g.slug = :groupSlug AND r.role = :role")
    Set<AppUser> findAllMembers(String institutionSlug, String groupSlug, AppRole role);

    @Query("SELECT u.email " +
            "FROM UserRole r " +
            "JOIN r.user u " +
            "JOIN r.group g " +
            "JOIN g.institution i " +
            "WHERE g.institution.slug = :institutionSlug AND g.slug = :groupSlug AND r.role = :role")
    Set<String> findAllMembersEmails(String institutionSlug, String groupSlug, AppRole role);
}
