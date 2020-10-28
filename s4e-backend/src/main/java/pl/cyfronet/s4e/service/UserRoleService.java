package pl.cyfronet.s4e.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.cyfronet.s4e.bean.AppRole;
import pl.cyfronet.s4e.bean.AppUser;
import pl.cyfronet.s4e.bean.Institution;
import pl.cyfronet.s4e.bean.UserRole;
import pl.cyfronet.s4e.controller.request.CreateUserRoleRequest;
import pl.cyfronet.s4e.controller.request.DeleteUserRoleRequest;
import pl.cyfronet.s4e.data.repository.AppUserRepository;
import pl.cyfronet.s4e.data.repository.InstitutionRepository;
import pl.cyfronet.s4e.data.repository.UserRoleRepository;
import pl.cyfronet.s4e.ex.NotFoundException;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserRoleService {
    private final UserRoleRepository userRoleRepository;
    private final AppUserRepository appUserRepository;
    private final InstitutionRepository institutionRepository;

    @Transactional(rollbackFor = NotFoundException.class)
    public void addRole(CreateUserRoleRequest request) throws NotFoundException {
        Institution institution = institutionRepository.findBySlug(request.getInstitutionSlug(), Institution.class)
                .orElseThrow(() -> new NotFoundException(
                        "Institution not found for id " + request.getInstitutionSlug() + "'"
                ));
        AppUser appUser = appUserRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new NotFoundException("User not found for mail: '" + request.getEmail() + "'"));
        addRole(institution, appUser, request.getRole());
    }

    @Transactional(rollbackFor = NotFoundException.class)
    public void addRole(String institutionSlug, Long appUserId, AppRole role) throws NotFoundException {
        val appUser = appUserRepository.findById(appUserId)
                .orElseThrow(() -> new NotFoundException("User not found for id: '" + appUserId + "'"));
        val institution = institutionRepository.findBySlug(institutionSlug, Institution.class)
                .orElseThrow(() -> new NotFoundException("Institution not found for slug " + institutionSlug + "'"));
        addRole(institution, appUser, role);
    }

    private void addRole(Institution institution, AppUser appUser, AppRole role) {
        {
            val optionalUserRole =
                    userRoleRepository.findByUser_IdAndInstitution_IdAndRole(appUser.getId(), institution.getId(), role);

            if (optionalUserRole.isPresent()) {
                return;
            }

            UserRole userRole = UserRole.builder()
                    .institution(institution)
                    .user(appUser)
                    .role(role)
                    .build();

            institution.addMemberRole(userRole);
            appUser.addRole(userRole);
        }

        // If role is admin, then try to add an ordinary membership and propagate the admin down the hierarchy.
        if (role == AppRole.INST_ADMIN) {
            addRole(institution, appUser, AppRole.INST_MEMBER);
            for (val childInstitution : institutionRepository.findAllByParentId(institution.getId())) {
                addRole(childInstitution, appUser, AppRole.INST_ADMIN);
            }
        }
    }

    @Transactional(rollbackFor = NotFoundException.class)
    public void removeRole(DeleteUserRoleRequest request) throws NotFoundException {
        Institution institution = institutionRepository.findBySlug(request.getInstitutionSlug())
                .orElseThrow(() -> new NotFoundException(
                        "Institution not found for id " + request.getInstitutionSlug() + "'"
                ));
        AppUser appUser = appUserRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new NotFoundException("User not found for mail: '" + request.getEmail() + "'"));
        removeRole(institution.getId(), appUser.getId(), request.getRole());
    }

    @Transactional(rollbackFor = NotFoundException.class)
    public void removeRole(
            String institutionSlug,
            Long appUserId,
            AppRole role
    ) throws NotFoundException {
        val appUser = appUserRepository.findById(appUserId)
                .orElseThrow(() -> new NotFoundException("User not found for id: '" + appUserId + "'"));
        val institution = institutionRepository.findBySlug(institutionSlug, Institution.class)
                .orElseThrow(() -> new NotFoundException("Institution not found for slug " + institutionSlug + "'"));
        removeRole(institution.getId(), appUser.getId(), role);
    }

    private void removeRole(Long institutionId, Long appUserId, AppRole role) {
        {
            val optionalUserRole =
                    userRoleRepository.findByUser_IdAndInstitution_IdAndRole(appUserId, institutionId, role);

            if (optionalUserRole.isEmpty()) {
                return;
            }

            UserRole userRole = optionalUserRole.get();

            userRole.getInstitution().removeMemberRole(userRole);
            userRole.getUser().removeRole(userRole);
        }

        // Try removing admin role as well, if exists.
        if (role == AppRole.INST_MEMBER) {
            removeRole(institutionId, appUserId, AppRole.INST_ADMIN);
        // If removing an admin role then propagate this deletion down the institution hierarchy.
        } else if (role == AppRole.INST_ADMIN) {
            for (val childInstitution : institutionRepository.findAllByParentId(institutionId)) {
                removeRole(childInstitution.getId(), appUserId, AppRole.INST_ADMIN);
            }
        }
    }
}
