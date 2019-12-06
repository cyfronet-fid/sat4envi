package pl.cyfronet.s4e.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.cyfronet.s4e.bean.AppUser;
import pl.cyfronet.s4e.bean.Group;
import pl.cyfronet.s4e.data.repository.AppUserRepository;
import pl.cyfronet.s4e.data.repository.GroupRepository;
import pl.cyfronet.s4e.ex.GroupCreationException;
import pl.cyfronet.s4e.ex.GroupUpdateException;
import pl.cyfronet.s4e.ex.NotFoundException;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class GroupService {
    private final GroupRepository groupRepository;
    private final AppUserRepository appUserRepository;

    @Transactional(rollbackFor = GroupCreationException.class)
    public Group save(Group group) throws GroupCreationException {
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
        return groupRepository.findAllMembers(institutionSlug, groupSlug);
    }

    public Page<Group> getAllByInstitution(String institutionSlug, Pageable pageable) {
        return groupRepository.findAllByInstitution_Slug(institutionSlug, pageable);
    }

    @Transactional
    public void updateUsersGroups(Long appUserId, String institutionSlug, Set<String> groupSlugs) throws NotFoundException {
        Set<Group> targetGroups = groupSlugs.stream()
                .flatMap(groupSlug -> groupRepository.findByInstitution_SlugAndSlug(institutionSlug, groupSlug).stream())
                .collect(Collectors.toSet());
        AppUser appUser = appUserRepository.findById(appUserId)
                .orElseThrow(() -> new NotFoundException("User not found for id: " + appUserId));
        Set<Group> userGroups = groupRepository.findAllByInstitution_SlugAndMembers_Email(institutionSlug, appUser.getEmail());
        removeFromGroups(targetGroups, appUser, userGroups);
        addToGroup(targetGroups, appUser, userGroups);
    }

    private void addToGroup(Set<Group> groupsFromRequest, AppUser user, Set<Group> userGroups) {
        for (Group group: groupsFromRequest) {
            if (!userGroups.contains(group)) {
                group.addMember(user);
                user.getGroups().add(group);
            }
        }
    }

    private void removeFromGroups(Set<Group> groupsFromRequest, AppUser user, Set<Group> userGroups) {
        for (Group group: userGroups) {
            if (!groupsFromRequest.contains(group)) {
                group.removeMember(user);
                user.getGroups().remove(group);
            }
        }
    }

    @Transactional
    public void addMember(String institutionSlug, String groupSlug, String email) {
        Group group = groupRepository.findByInstitution_SlugAndSlug(institutionSlug, groupSlug).get();
        AppUser appUser = appUserRepository.findByEmail(email).get();
        group.addMember(appUser);
        appUser.getGroups().add(group);
    }

    @Transactional
    public void removeMember(String institutionSlug, String groupSlug, String email) throws NotFoundException {
        Group group = groupRepository.findByInstitution_SlugAndSlug(institutionSlug, groupSlug)
                .orElseThrow(() -> new NotFoundException("Group not found for id " + groupSlug));
        AppUser appUser = appUserRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found for mail: '" + email + "'"));
        group.removeMember(appUser);
        appUser.getGroups().remove(group);
    }

    @Transactional
    public void delete(Group group) {
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
}
