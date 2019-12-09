package pl.cyfronet.s4e.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.cyfronet.s4e.bean.*;
import pl.cyfronet.s4e.controller.request.CreateGroupRequest;
import pl.cyfronet.s4e.controller.request.UpdateGroupRequest;
import pl.cyfronet.s4e.controller.request.UpdateUserGroupsRequest;
import pl.cyfronet.s4e.data.repository.AppUserRepository;
import pl.cyfronet.s4e.data.repository.GroupRepository;
import pl.cyfronet.s4e.data.repository.UserRoleRepository;
import pl.cyfronet.s4e.event.OnAddToGroupEvent;
import pl.cyfronet.s4e.event.OnRemoveFromGroupEvent;
import pl.cyfronet.s4e.ex.GroupCreationException;
import pl.cyfronet.s4e.ex.GroupUpdateException;
import pl.cyfronet.s4e.ex.NotFoundException;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class GroupService {
    private final GroupRepository groupRepository;
    private final AppUserRepository appUserRepository;
    private final UserRoleRepository userRoleRepository;
    private final SlugService slugService;
    private final InstitutionService institutionService;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional(rollbackFor = GroupCreationException.class)
    public Group save(Group group) throws GroupCreationException {
        try {
            return groupRepository.save(group);
        } catch (DataIntegrityViolationException e) {
            log.info("Cannot create Group with name '" + group.getName() + "'", e);
            throw new GroupCreationException("Cannot create Group", e);
        }
    }

    @Transactional(rollbackFor = GroupCreationException.class)
    public Group createFromRequest(CreateGroupRequest request, String institutionSlug)
            throws GroupCreationException, NotFoundException {
        val institution = institutionService.getInstitution(institutionSlug)
                .orElseThrow(() -> new NotFoundException("Institution not found for id '" + institutionSlug + "'"));
        Group group = Group.builder()
                .name(request.getName())
                .slug(slugService.slugify(request.getName()))
                .institution(institution)
                .build();

        if (request.getMembersEmails() != null) {
            for (String memberEmail : request.getMembersEmails()) {
                group.getMembersRoles().add(UserRole.builder()
                        .role(AppRole.GROUP_MEMBER)
                        .user(appUserRepository.findByEmail(memberEmail)
                                .orElseThrow(() -> new NotFoundException("User not found for email '" + memberEmail + "'")))
                        .group(group)
                        .build());
            }
        }

        try {
            return groupRepository.save(group);
        } catch (DataIntegrityViolationException e) {
            log.info("Cannot create Group with name '" + group.getName() + "'", e);
            throw new GroupCreationException("Cannot create Group", e);
        }
    }

    public Optional<Group> getGroup(String institutionSlug, String groupSlug) {
        return groupRepository.findByInstitution_SlugAndSlug(institutionSlug, groupSlug);
    }

    public Set<AppUser> getMembers(String institutionSlug, String groupSlug) {
        return groupRepository.findAllMembers(institutionSlug, groupSlug, AppRole.GROUP_MEMBER);
    }

    public Page<Group> getAllByInstitution(String institutionSlug, Pageable pageable) {
        return groupRepository.findAllByInstitution_Slug(institutionSlug, pageable);
    }

    @Transactional
    public void updateUserGroups(UpdateUserGroupsRequest request, String institutionSlug) throws NotFoundException {
        Set<Group> targetGroups = request.getGroupSlugs().stream()
                .flatMap(groupSlug -> groupRepository.findByInstitution_SlugAndSlug(institutionSlug, groupSlug).stream())
                .collect(Collectors.toSet());
        AppUser appUser = appUserRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new NotFoundException("User not found for id: " + request.getEmail() + "'"));
        Set<Group> userGroups = groupRepository.findAllByInstitutionAndMemberEmail(institutionSlug, appUser.getEmail(), AppRole.GROUP_MEMBER);
        removeFromGroups(targetGroups, appUser, userGroups);
        addToGroup(targetGroups, appUser, userGroups);
    }

    private void addToGroup(Set<Group> groupsFromRequest, AppUser user, Set<Group> userGroups) {
        if (!groupsFromRequest.isEmpty()) {
            for (Group group : groupsFromRequest) {
                if (!userGroups.contains(group)) {
                    UserRole userRole = UserRole.builder().role(AppRole.GROUP_MEMBER).user(user).group(group).build();
                    group.getMembersRoles().add(userRole);
                    user.getRoles().add(userRole);
                }
            }
        }
    }

    private void removeFromGroups(Set<Group> groupsFromRequest, AppUser user, Set<Group> userGroups) {
        for (Group group : userGroups) {
            if (!groupsFromRequest.contains(group)) {
                userRoleRepository.findByUser_IdAndGroup_Id(user.getId(), group.getId()).forEach(userRole -> {
                    user.removeRole(userRole);
                    group.removeMemberRole(userRole);
                    userRoleRepository.delete(userRole);
                });
            }
        }
    }

    @Transactional
    public void addMember(String institutionSlug, String groupSlug, String email) throws NotFoundException {
        Group group = groupRepository.findByInstitution_SlugAndSlug(institutionSlug, groupSlug)
                .orElseThrow(() -> new NotFoundException("Group not found for id " + groupSlug + "'"));
        AppUser appUser = appUserRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found for mail: '" + email + "'"));
        UserRole userRole = UserRole.builder().role(AppRole.GROUP_MEMBER).user(appUser).group(group).build();
        group.getMembersRoles().add(userRole);
        appUser.getRoles().add(userRole);
        eventPublisher.publishEvent(new OnAddToGroupEvent(appUser, group, LocaleContextHolder.getLocale()));
    }

    @Transactional
    public void removeMember(String institutionSlug, String groupSlug, String email) throws NotFoundException {
        Group group = groupRepository.findByInstitution_SlugAndSlug(institutionSlug, groupSlug)
                .orElseThrow(() -> new NotFoundException("Group not found for id " + groupSlug + "'"));
        AppUser appUser = appUserRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found for mail: '" + email + "'"));
        userRoleRepository.findByUser_IdAndGroup_Id(appUser.getId(), group.getId()).forEach(userRole -> {
            group.removeMemberRole(userRole);
            appUser.removeRole(userRole);
            userRoleRepository.delete(userRole);
        });

        eventPublisher.publishEvent(new OnRemoveFromGroupEvent(appUser, group, LocaleContextHolder.getLocale()));
    }

    @Transactional
    public void delete(Group group) {
        groupRepository.deleteById(group.getId());
    }

    @Transactional
    public void deleteBySlugs(String institutionSlug, String groupSlug) throws NotFoundException {
        val group = getGroup(institutionSlug, groupSlug)
                .orElseThrow(() -> new NotFoundException("Group not found for id '" + groupSlug + "'"));
        groupRepository.deleteById(group.getId());
    }

    @Transactional(rollbackFor = GroupUpdateException.class)
    public void update(Group group) throws GroupUpdateException {
        try {
            groupRepository.save(group);
        } catch (DataIntegrityViolationException e) {
            log.info("Cannot update Group with name '" + group.getName() + "'", e);
            throw new GroupUpdateException("Cannot update Group", e);
        }
    }

    @Transactional(rollbackFor = GroupUpdateException.class)
    public void updateFromRequest(UpdateGroupRequest request, String institutionSlug, String groupSlug) throws NotFoundException {
        Set<String> membersToRemove = groupRepository.findAllMembersEmails(institutionSlug, groupSlug, AppRole.GROUP_MEMBER);

        Group group = getGroup(institutionSlug, groupSlug)
                .orElseThrow(() -> new NotFoundException("Group not found for id '" + groupSlug + "'"));
        group.setName(request.getName());
        group.setSlug(slugService.slugify(request.getName()));
        if (request.getMembersEmails() != null) {
            Set<UserRole> updatedMemberRoles = new HashSet<>();
            membersToRemove.removeAll(request.getMembersEmails());
            for (String memberEmail : request.getMembersEmails()) {
                AppUser user = appUserRepository.findByEmail(memberEmail)
                        .orElseThrow(() -> new NotFoundException("User not found for email '" + memberEmail + "'"));
                updatedMemberRoles.add(userRoleRepository.findByUser_IdAndGroup_IdAndRole(
                        user.getId(),
                        group.getId(),
                        AppRole.GROUP_MEMBER)
                        .orElse(UserRole.builder()
                                .role(AppRole.GROUP_MEMBER)
                                .user(user)
                                .group(group)
                                .build()));
            }
            //remove userrole from user
            for (String memberEmail : membersToRemove) {
                AppUser user = appUserRepository.findByEmail(memberEmail)
                        .orElseThrow(() -> new NotFoundException("User not found for email '" + memberEmail + "'"));
                userRoleRepository.findByUser_IdAndGroup_Id(user.getId(), group.getId()).forEach(userRole -> {
                    user.removeRole(userRole);
                    userRoleRepository.delete(userRole);
                });
            }
            group.setMembersRoles(updatedMemberRoles);
        }
    }
}
