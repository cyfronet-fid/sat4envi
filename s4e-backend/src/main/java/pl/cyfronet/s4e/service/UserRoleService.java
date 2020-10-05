package pl.cyfronet.s4e.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
        addRole(request.getRole(), request.getEmail(), request.getInstitutionSlug());
    }

    @Transactional(rollbackFor = NotFoundException.class)
    public void addRole(AppRole role, String email, String institutionSlug) throws NotFoundException {
        Institution institution = institutionRepository.findBySlug(institutionSlug, Institution.class)
                .orElseThrow(() -> new NotFoundException("Institution not found for id " + institutionSlug + "'"));
        AppUser appUser = appUserRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found for mail: '" + email + "'"));
        if (!AppRole.INST_MEMBER.equals(role) &&
                userRoleRepository.findByUser_IdAndInstitution_IdAndRole(appUser.getId(), institution.getId(), AppRole.INST_MEMBER).isEmpty()) {
            UserRole userRole = UserRole.builder().
                    role(AppRole.INST_MEMBER)
                    .user(appUser)
                    .institution(institution)
                    .build();
            institution.getMembersRoles().add(userRole);
            appUser.getRoles().add(userRole);
        }
        UserRole userRole = UserRole.builder().
                role(role)
                .user(appUser)
                .institution(institution)
                .build();
        institution.getMembersRoles().add(userRole);
        appUser.getRoles().add(userRole);
    }

    @Transactional(rollbackFor = NotFoundException.class)
    public void removeRole(DeleteUserRoleRequest request) throws NotFoundException {
        removeRole(request.getRole(), request.getEmail(), request.getInstitutionSlug());
    }

    @Transactional(rollbackFor = NotFoundException.class)
    public void removeRole(AppRole role, String email, String institutionSlug) throws NotFoundException {
        Institution institution = institutionRepository.findBySlug(institutionSlug, Institution.class)
                .orElseThrow(() -> new NotFoundException("Institution not found for id " + institutionSlug + "'"));
        AppUser appUser = appUserRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found for mail: '" + email + "'"));
        if (AppRole.INST_MEMBER.equals(role)) {
            userRoleRepository.findByUser_IdAndInstitution_Id(appUser.getId(), institution.getId()).forEach(userRole -> {
                institution.removeMemberRole(userRole);
                appUser.removeRole(userRole);
                userRoleRepository.delete(userRole);
            });
        } else {
            UserRole userRole = userRoleRepository.findByUser_IdAndInstitution_IdAndRole(appUser.getId(), institution.getId(), role)
                    .orElseThrow(() -> new NotFoundException("UserRole not found for user: '" + email + "'"));
            institution.removeMemberRole(userRole);
            appUser.removeRole(userRole);
            userRoleRepository.delete(userRole);
        }

    }
}
