package pl.cyfronet.s4e.data.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;
import pl.cyfronet.s4e.bean.AppRole;
import pl.cyfronet.s4e.bean.UserRole;

import java.util.Optional;
import java.util.Set;

@Transactional(readOnly = true)
public interface UserRoleRepository extends CrudRepository<UserRole, Long> {
    Optional<UserRole> findByUser_IdAndGroup_IdAndRole(Long userId, Long groupId, AppRole role);
    Set<UserRole> findByUser_IdAndGroup_Id(Long userId, Long groupId);
}
