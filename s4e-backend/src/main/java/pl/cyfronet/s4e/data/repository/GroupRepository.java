package pl.cyfronet.s4e.data.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;
import pl.cyfronet.s4e.bean.AppUser;
import pl.cyfronet.s4e.bean.Group;

import java.util.Optional;
import java.util.Set;

@Transactional(readOnly = true)
public interface GroupRepository extends CrudRepository<Group, Long> {
    Page<Group> findAllByInstitution_Slug(String slug, Pageable pageable);

    Optional<Group> findByInstitution_SlugAndSlug(String institutionSlug, String slug);

    Set<Group> findAllByInstitution_SlugAndMembers_Email(String institutionSlug, String email);

    @Query("SELECT u " +
            "FROM AppUser u " +
            "JOIN FETCH u.groups g " +
            "JOIN FETCH g.institution i " +
            "WHERE g.institution.slug = :institutionSlug AND g.slug = :groupSlug")
    Set<AppUser> findAllMembers(String institutionSlug, String groupSlug);
}
