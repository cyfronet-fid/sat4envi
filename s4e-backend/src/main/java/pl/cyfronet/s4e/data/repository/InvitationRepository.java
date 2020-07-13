package pl.cyfronet.s4e.data.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;
import pl.cyfronet.s4e.bean.Invitation;

import java.util.Optional;
import java.util.Set;

@Transactional(readOnly = true)
public interface InvitationRepository extends CrudRepository<Invitation, Long> {
    <T> Optional<T> findByEmailAndInstitutionSlug(String email, String institutionSlug, Class<T> projection);
    <T> Optional<T> findByToken(String token, Class<T> projection);
    <T> Optional<T> findById(Long id, Class<T> projection);
    <T> Set<T> findAllByInstitutionSlug(String institutionSlug, Class<T> projection);
}
