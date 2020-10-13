package pl.cyfronet.s4e.data.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;
import pl.cyfronet.s4e.bean.AppUser;

import java.util.Optional;

@Transactional(readOnly = true)
public interface AppUserRepository extends CrudRepository<AppUser, Long> {
    Optional<AppUser> findByEmail(String email);

    @Query("SELECT u " +
            "FROM AppUser u " +
            "LEFT JOIN FETCH u.roles r " +
            "LEFT JOIN FETCH r.institution i " +
            "WHERE u.email = :email")
    Optional<AppUser> findByEmailWithRolesAndInstitutions(String email);

    @Query("SELECT u " +
            "FROM AppUser u " +
            "LEFT JOIN FETCH u.roles r " +
            "LEFT JOIN FETCH r.institution i " +
            "WHERE u.email = :email")
    <T> Optional<T> findByEmailWithRolesAndInstitution(String email, Class<T> projection);

    boolean existsByEmail(String email);
}
