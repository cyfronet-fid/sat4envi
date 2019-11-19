package pl.cyfronet.s4e.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.cyfronet.s4e.bean.AppUser;
import pl.cyfronet.s4e.bean.Group;
import pl.cyfronet.s4e.data.repository.GroupRepository;
import pl.cyfronet.s4e.ex.GroupCreationException;
import pl.cyfronet.s4e.ex.GroupUpdateException;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class GroupService {
    private final GroupRepository groupRepository;

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
        return groupRepository.findBySlugAndInstitution_Slug(groupSlug, institutionSlug);
    }

    @Transactional
    public Set<AppUser> getMembers(String institutionSlug, String groupSlug) {
        val optionalGroup = getGroup(institutionSlug, groupSlug);
        if (optionalGroup.isEmpty()) return new HashSet<>();
        if (optionalGroup.get().getMembers().isEmpty()) return new HashSet<>();
        return optionalGroup.get().getMembers();
    }

    public Page<Group> getAllByInstitution(String institutionSlug, Pageable pageable) {
        return groupRepository.findAllByInstitution_Slug(institutionSlug, pageable);
    }

    public Set<Group> getAllGroupsBySlugs(Set<String> groupSlugs, String institutionSlug) {
        return groupRepository.findAllByInstitution_SlugAndSlugIn(institutionSlug, groupSlugs);
    }

    @Transactional(rollbackFor = GroupUpdateException.class)
    public void updateUsersGroups(Set<Group> groupsFromRequest, AppUser user, String institutionSlug) throws GroupUpdateException {
        Set<Group> userGroups = groupRepository.findAllByInstitution_SlugAndMembers_Email(institutionSlug, user.getEmail());
        removeFromGroups(groupsFromRequest, user, userGroups);
        addToGroup(groupsFromRequest, user, userGroups);
    }

    private void addToGroup(Set<Group> groupsFromRequest, AppUser user, Set<Group> userGroups) throws GroupUpdateException {
        for (Group group : groupsFromRequest) {
            if (!userGroups.contains(group)) {
                group.addMember(user);
                update(group);
            }
        }
    }

    private void removeFromGroups(Set<Group> groupsFromRequest, AppUser user, Set<Group> userGroups) throws GroupUpdateException {
        for (Group group : userGroups) {
            if (!groupsFromRequest.contains(group)) {
                group.removeMember(user);
                update(group);
            }
        }
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
