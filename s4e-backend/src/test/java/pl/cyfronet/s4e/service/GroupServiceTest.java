package pl.cyfronet.s4e.service;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import pl.cyfronet.s4e.BasicTest;
import pl.cyfronet.s4e.TestDbHelper;
import pl.cyfronet.s4e.bean.*;
import pl.cyfronet.s4e.controller.request.UpdateUserGroupsRequest;
import pl.cyfronet.s4e.data.repository.InstitutionRepository;
import pl.cyfronet.s4e.ex.AppUserCreationException;
import pl.cyfronet.s4e.ex.GroupCreationException;
import pl.cyfronet.s4e.ex.GroupUpdateException;
import pl.cyfronet.s4e.ex.InstitutionCreationException;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static com.github.npathai.hamcrestopt.OptionalMatchers.isEmpty;
import static com.github.npathai.hamcrestopt.OptionalMatchers.isPresent;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@BasicTest
@Slf4j
public class GroupServiceTest {
    @Autowired
    private InstitutionService institutionService;

    @Autowired
    private GroupService groupService;

    @Autowired
    private AppUserService appUserService;

    @Autowired
    private InstitutionRepository institutionRepository;

    @Autowired
    private TestDbHelper testDbHelper;

    @BeforeEach
    public void setUp() {
        reset();
    }

    @AfterEach
    void tearDown() {
        reset();
    }

    private void reset() {
        testDbHelper.clean();
    }

    @Test
    public void shouldDeleteOnlyGroup() throws InstitutionCreationException, GroupCreationException {
        Institution institution = Institution.builder().name("Instytycja 12").slug("instytucja-12").build();
        institutionService.save(institution);

        Group group = Group.builder().name("Group 13").slug("group-13").institution(institution).build();
        Group group2 = Group.builder().name("Group 14").slug("group-14").institution(institution).build();
        groupService.save(group);
        groupService.save(group2);

        val groupDelete = groupService.getGroup("instytucja-12", "group-13", Group.class);
        groupService.delete(groupDelete.get());

        assertThat(groupService.getGroup("instytucja-12", "group-13", Group.class), isEmpty());
        assertThat(groupService.getGroup("instytucja-12", "group-14", Group.class), isPresent());
        assertThat(institutionService.findBySlug("instytucja-12", Institution.class), isPresent());
    }

    @Test
    public void shouldDeleteGroupAndMembers() throws InstitutionCreationException, GroupCreationException, AppUserCreationException, GroupUpdateException {
        Institution institution = Institution.builder().name("Instytycja 12").slug("instytucja-12").build();
        institutionService.save(institution);
        Group group = groupService.save(Group.builder().name("Group 13").slug("group-13").institution(institution).build());
        AppUser user = appUserService.save(AppUser.builder()
                .email("mail@test.pl")
                .name("Name")
                .surname("Surname")
                .password("someHash")
                .build());
        group.getMembersRoles().add(buildGroupMemberUserRole(user, group));
        groupService.update(group);
        Set<AppUser> members = groupService.getMembers("instytucja-12", "group-13", AppUser.class);

        assertThat(members, not(empty()));
        assertThat(groupService.getGroup("instytucja-12", "group-13", Group.class), isPresent());
        assertThat(institutionRepository.findBySlug("instytucja-12", Institution.class), isPresent());
        assertThat(appUserService.findByEmail("mail@test.pl"), isPresent());

        val groupDB = groupService.getGroup("instytucja-12", "group-13", Group.class);
        groupService.delete(groupDB.get());
        Set<AppUser> members2 = groupService.getMembers("instytucja-12", "group-13", AppUser.class);

        assertThat(members2, empty());
        assertThat(groupService.getGroup("instytucja-12", "group-13", Group.class), isEmpty());
        assertThat(institutionRepository.findBySlug("instytucja-12", Institution.class), isPresent());
        assertThat(appUserService.findByEmail("mail@test.pl"), isPresent());
    }

    @Test
    public void shouldUpdateUsersGroupsForUserWithNoGroups() throws Exception {
        appUserService.save(AppUser.builder()
                .email("mail@test.pl")
                .name("Name")
                .surname("Surname")
                .password("someHash")
                .build());
        Institution institution = Institution.builder().name("Instytycja 12").slug("instytucja-12").build();
        institutionService.save(institution);

        groupService.save(Group.builder().name("Group 13").slug("group-13").institution(institution).build());
        groupService.save(Group.builder().name("Group 14").slug("group-14").institution(institution).build());
        groupService.save(Group.builder().name("Group 15").slug("group-15").institution(institution).build());

        assertThat(groupService.getMembers("instytucja-12", "group-13", AppUser.class), empty());
        assertThat(groupService.getMembers("instytucja-12", "group-14", AppUser.class), empty());
        assertThat(groupService.getMembers("instytucja-12", "group-15", AppUser.class), empty());

        Map<String, Set<AppRole>> groupsWithRoles = new HashMap<>();
        groupsWithRoles.put("group-13", Set.of(AppRole.GROUP_MEMBER));
        groupsWithRoles.put("group-14", Set.of(AppRole.GROUP_MEMBER));
        groupsWithRoles.put("group-15", Set.of(AppRole.GROUP_MEMBER));
        val user = appUserService.findByEmail("mail@test.pl").get();

        UpdateUserGroupsRequest request = UpdateUserGroupsRequest.builder()
                .email(user.getEmail())
                .groupsWithRoles(groupsWithRoles)
                .build();
        groupService.updateUserGroups(request, "instytucja-12");

        assertThat(groupService.getMembers("instytucja-12", "group-13", AppUser.class), hasSize(1));
        assertThat(groupService.getMembers("instytucja-12", "group-14", AppUser.class), hasSize(1));
        assertThat(groupService.getMembers("instytucja-12", "group-15", AppUser.class), hasSize(1));
    }

    @Test
    public void shouldUpdateUsersGroupsAndRemoveOne() throws Exception {
        AppUser user = appUserService.save(AppUser.builder()
                .email("mail@test.pl")
                .name("Name")
                .surname("Surname")
                .password("someHash")
                .build());

        Institution institution = Institution.builder().name("Instytycja 12").slug("instytucja-12").build();
        institutionService.save(institution);

        assertThat(groupService.getMembers("instytucja-12", "group-13", AppUser.class), empty());
        assertThat(groupService.getMembers("instytucja-12", "group-14", AppUser.class), empty());
        assertThat(groupService.getMembers("instytucja-12", "group-15", AppUser.class), empty());

        Group group13 = groupService.save(Group.builder()
                .name("Group 13")
                .slug("group-13")
                .institution(institution)
                .build());
        group13.getMembersRoles().add(buildGroupMemberUserRole(user, group13));
        groupService.update(group13);

        Group group14 = groupService.save(Group.builder()
                .name("Group 14")
                .slug("group-14")
                .institution(institution)
                .build());
        group14.getMembersRoles().add(buildGroupMemberUserRole(user, group14));
        groupService.update(group14);

        Group group15 = groupService.save(Group.builder()
                .name("Group 15")
                .slug("group-15")
                .institution(institution)
                .build());
        group15.getMembersRoles().add(buildGroupMemberUserRole(user, group15));
        groupService.update(group15);

        assertThat(groupService.getMembers("instytucja-12", "group-13", AppUser.class), hasSize(1));
        assertThat(groupService.getMembers("instytucja-12", "group-14", AppUser.class), hasSize(1));
        assertThat(groupService.getMembers("instytucja-12", "group-15", AppUser.class), hasSize(1));


        Map<String, Set<AppRole>> groupsWithRoles = new HashMap<>();
        groupsWithRoles.put("group-13", Set.of(AppRole.GROUP_MEMBER));
        groupsWithRoles.put("group-14", Set.of(AppRole.GROUP_MEMBER));

        UpdateUserGroupsRequest request = UpdateUserGroupsRequest.builder()
                .email(user.getEmail())
                .groupsWithRoles(groupsWithRoles)
                .build();
        groupService.updateUserGroups(request, "instytucja-12");

        assertThat(groupService.getMembers("instytucja-12", "group-13", AppUser.class), hasSize(1));
        assertThat(groupService.getMembers("instytucja-12", "group-14", AppUser.class), hasSize(1));
        assertThat(groupService.getMembers("instytucja-12", "group-15", AppUser.class), empty());
    }

    @Test
    public void shouldUpdateUsersGroupsAndRemoveAll() throws Exception {
        AppUser user = appUserService.save(AppUser.builder()
                .email("mail@test.pl")
                .name("Name")
                .surname("Surname")
                .password("someHash")
                .build());

        Institution institution = Institution.builder().name("Instytycja 12").slug("instytucja-12").build();
        institutionService.save(institution);

        Group group13 = groupService.save(Group.builder()
                .name("Group 13").slug("group-13").institution(institution).build());
        Group group14 = groupService.save(Group.builder().name("Group 14").slug("group-14").institution(institution).build());
        Group group15 = groupService.save(Group.builder().name("Group 15").slug("group-15").institution(institution).build());
        group13.getMembersRoles().add(buildGroupMemberUserRole(user, group13));
        group14.getMembersRoles().add(buildGroupMemberUserRole(user, group14));
        group15.getMembersRoles().add(buildGroupMemberUserRole(user, group15));
        groupService.update(group13);
        groupService.update(group14);
        groupService.update(group15);

        assertThat(groupService.getMembers("instytucja-12", "group-13", AppUser.class), hasSize(1));
        assertThat(groupService.getMembers("instytucja-12", "group-14", AppUser.class), hasSize(1));
        assertThat(groupService.getMembers("instytucja-12", "group-15", AppUser.class), hasSize(1));

        UpdateUserGroupsRequest request = UpdateUserGroupsRequest.builder()
                .email(user.getEmail())
                .groupsWithRoles(Map.of())
                .build();
        groupService.updateUserGroups(request, "instytucja-12");

        assertThat(groupService.getMembers("instytucja-12", "group-13", AppUser.class), empty());
        assertThat(groupService.getMembers("instytucja-12", "group-14", AppUser.class), empty());
        assertThat(groupService.getMembers("instytucja-12", "group-15", AppUser.class), empty());
    }

    @Test
    public void shouldUpdateUsersGroupsIfUserInTwoInstitutionsAndRemoveOne() throws Exception {
        AppUser user = appUserService.save(AppUser.builder()
                .email("mail@test.pl")
                .name("Name")
                .surname("Surname")
                .password("someHash")
                .build());

        Institution institution = Institution.builder().name("Instytycja 12").slug("instytucja-12").build();
        institutionService.save(institution);

        Institution institution2 = Institution.builder().name("Instytycja 22").slug("instytucja-22").build();
        institutionService.save(institution2);

        assertThat(groupService.getMembers("instytucja-12", "group-13", AppUser.class), empty());

        Group group13 = groupService.save(Group.builder()
                .name("Group 13")
                .slug("group-13")
                .institution(institution)
                .build());
        group13.getMembersRoles().add(buildGroupMemberUserRole(user, group13));
        groupService.update(group13);

        assertThat(groupService.getMembers("instytucja-12", "group-13", AppUser.class), hasSize(1));

        Group group21 = groupService.save(Group.builder()
                .name("Group 21")
                .slug("group-21")
                .institution(institution2)
                .build());
        group21.getMembersRoles().add(buildGroupMemberUserRole(user, group21));
        groupService.update(group21);

        assertThat(groupService.getMembers("instytucja-22", "group-21", AppUser.class), hasSize(1));

        UpdateUserGroupsRequest request = UpdateUserGroupsRequest.builder()
                .email(user.getEmail())
                .groupsWithRoles(Map.of("group-21", Set.of(AppRole.GROUP_MEMBER)))
                .build();
        groupService.updateUserGroups(request, "instytucja-22");

        assertThat(groupService.getMembers("instytucja-12", "group-13", AppUser.class), empty());
        assertThat(groupService.getMembers("instytucja-22", "group-21", AppUser.class), hasSize(1));
    }

    private UserRole buildGroupMemberUserRole(AppUser user, Group group) {
        return UserRole.builder()
                .role(AppRole.GROUP_MEMBER)
                .user(user)
                .group(group)
                .build();

    }
}
