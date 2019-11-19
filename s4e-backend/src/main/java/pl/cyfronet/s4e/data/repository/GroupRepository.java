package pl.cyfronet.s4e.data.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import pl.cyfronet.s4e.bean.Group;

import java.util.Optional;
import java.util.Set;

public interface GroupRepository extends CrudRepository<Group, Long> {
    Page<Group> findAllByInstitution_Slug(String slug, Pageable pageable);

    Optional<Group> findBySlugAndInstitution_Slug(String slug, String institutionSlug);

    Set<Group> findAllByInstitution_SlugAndSlugIn(String institutionSlug, Set<String> slugs);

    Set<Group> findAllByInstitution_SlugAndMembers_Email(String institutionSlug, String email);
}
