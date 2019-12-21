package pl.cyfronet.s4e.data.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;
import pl.cyfronet.s4e.bean.AppRole;
import pl.cyfronet.s4e.bean.UserRole;

import java.util.Optional;
import java.util.Set;

@Transactional(readOnly = true)
public interface UserRoleRepository extends CrudRepository<UserRole, Long> {
    Optional<UserRole> findByUser_IdAndGroup_IdAndRole(Long userId, Long groupId, AppRole role);
    @Query(value = "SELECT r " +
            "FROM UserRole r " +
            "LEFT JOIN r.user u " +
            "LEFT JOIN r.group g " +
            "LEFT JOIN g.institution i " +
            "WHERE i.slug = :institutionSlug AND u.email = :email")
    Set<UserRole> findUserRolesInInstitution(String email, String institutionSlug);
    @Query(value = "SELECT r " +
            "FROM UserRole r " +
            "LEFT JOIN r.user u " +
            "LEFT JOIN r.group g " +
            "LEFT JOIN g.institution i " +
            "WHERE i.slug = :institutionSlug AND u.email = :email AND g.slug = :groupSlug AND r.role = :role")
    Optional<UserRole>  findUserRolesInInstitution(String email, String institutionSlug, String groupSlug, AppRole role);
    @Query(value = "SELECT r " +
            "FROM UserRole r " +
            "LEFT JOIN r.user u " +
            "LEFT JOIN FETCH r.group g " +
            "LEFT JOIN g.institution i " +
            "WHERE u.id = :userId")
    Set<UserRole> findByUser_Id(Long userId);
    Set<UserRole> findByUser_IdAndGroup_Id(Long userId, Long groupId);

}
