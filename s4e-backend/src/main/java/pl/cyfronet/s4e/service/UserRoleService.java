package pl.cyfronet.s4e.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.cyfronet.s4e.bean.AppRole;
import pl.cyfronet.s4e.bean.AppUser;
import pl.cyfronet.s4e.bean.Group;
import pl.cyfronet.s4e.bean.UserRole;
import pl.cyfronet.s4e.data.repository.AppUserRepository;
import pl.cyfronet.s4e.data.repository.GroupRepository;
import pl.cyfronet.s4e.data.repository.UserRoleRepository;
import pl.cyfronet.s4e.ex.NotFoundException;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserRoleService {
    private final UserRoleRepository userRoleRepository;
    private final AppUserRepository appUserRepository;
    private final GroupRepository groupRepository;

    @Transactional
    public void addRole(AppRole role, String email, String institutionSlug, String groupSlug) throws NotFoundException {
        Group group = groupRepository.findByInstitution_SlugAndSlug(institutionSlug, groupSlug)
                .orElseThrow(() -> new NotFoundException("Group not found for id " + groupSlug + "'"));
        AppUser appUser = appUserRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found for mail: '" + email + "'"));
        if (!AppRole.GROUP_MEMBER.equals(role) &&
            userRoleRepository.findByUser_IdAndGroup_IdAndRole(appUser.getId(), group.getId(), AppRole.GROUP_MEMBER).isEmpty())
        {
            UserRole userRole = UserRole.builder().
                    role(AppRole.GROUP_MEMBER)
                    .user(appUser)
                    .group(group)
                    .build();
            group.getMembersRoles().add(userRole);
            appUser.getRoles().add(userRole);
        }
        UserRole userRole = UserRole.builder().
                role(role)
                .user(appUser)
                .group(group)
                .build();
        group.getMembersRoles().add(userRole);
        appUser.getRoles().add(userRole);
    }

    @Transactional
    public void removeRole(AppRole role, String email, String institutionSlug, String groupSlug) throws NotFoundException {
        Group group = groupRepository.findByInstitution_SlugAndSlug(institutionSlug, groupSlug)
                .orElseThrow(() -> new NotFoundException("Group not found for id " + groupSlug + "'"));
        AppUser appUser = appUserRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found for mail: '" + email + "'"));
        if (AppRole.GROUP_MEMBER.equals(role)) {
            userRoleRepository.findByUser_IdAndGroup_Id(appUser.getId(), group.getId()).forEach(userRole -> {
                group.removeMemberRole(userRole);
                appUser.removeRole(userRole);
                userRoleRepository.delete(userRole);
            });
        } else {
            UserRole userRole = userRoleRepository.findByUser_IdAndGroup_IdAndRole(appUser.getId(), group.getId(), role)
                    .orElseThrow(() -> new NotFoundException("UserRole not found for user: '" + email + "'"));
            group.removeMemberRole(userRole);
            appUser.removeRole(userRole);
            userRoleRepository.delete(userRole);
        }

    }
}
