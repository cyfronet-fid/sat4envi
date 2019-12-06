package pl.cyfronet.s4e.service;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import pl.cyfronet.s4e.BasicTest;
import pl.cyfronet.s4e.bean.AppUser;
import pl.cyfronet.s4e.bean.Group;
import pl.cyfronet.s4e.bean.Institution;
import pl.cyfronet.s4e.data.repository.AppUserRepository;
import pl.cyfronet.s4e.data.repository.InstitutionRepository;
import pl.cyfronet.s4e.ex.AppUserCreationException;
import pl.cyfronet.s4e.ex.GroupCreationException;
import pl.cyfronet.s4e.ex.GroupUpdateException;
import pl.cyfronet.s4e.ex.InstitutionCreationException;

import java.util.Set;

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
    private AppUserRepository appUserRepository;

    @BeforeEach
    public void setUp() {
        institutionRepository.deleteAll();
        appUserRepository.deleteAll();
    }

    @Test
    public void shouldDeleteOnlyGroup() throws InstitutionCreationException, GroupCreationException {
        Institution institution = Institution.builder().name("Instytycja 12").slug("instytucja-12").build();
        institutionService.save(institution);

        Group group = Group.builder().name("Group 13").slug("group-13").institution(institution).build();
        Group group2 = Group.builder().name("Group 14").slug("group-14").institution(institution).build();
        groupService.save(group);
        groupService.save(group2);

        val groupDelete = groupService.getGroup("instytucja-12", "group-13");
        groupService.delete(groupDelete.get());

        assertThat(groupService.getGroup("instytucja-12", "group-13").isPresent(), is(false));
        assertThat(groupService.getGroup("instytucja-12", "group-14").isPresent(), is(true));
        assertThat(institutionService.getInstitution("instytucja-12").isPresent(), is(true));
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
        group.addMember(user);
        groupService.update(group);
        Set<AppUser> members = groupService.getMembers("instytucja-12", "group-13");

        assertThat(members, not(empty()));
        assertThat(groupService.getGroup("instytucja-12", "group-13").isPresent(), is(true));
        assertThat(institutionRepository.findBySlug("instytucja-12").isPresent(), is(true));
        assertThat(appUserService.findByEmail("mail@test.pl").isPresent(), is(true));

        val groupDB = groupService.getGroup("instytucja-12", "group-13");
        groupService.delete(groupDB.get());
        Set<AppUser> members2 = groupService.getMembers("instytucja-12", "group-13");

        assertThat(members2, empty());
        assertThat(groupService.getGroup("instytucja-12", "group-13").isPresent(), is(false));
        assertThat(institutionRepository.findBySlug("instytucja-12").isPresent(), is(true));
        assertThat(appUserService.findByEmail("mail@test.pl").isPresent(), is(true));
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

        assertThat(groupService.getMembers("instytucja-12", "group-13"), empty());
        assertThat(groupService.getMembers("instytucja-12", "group-14"), empty());
        assertThat(groupService.getMembers("instytucja-12", "group-15"), empty());

        Set<String> groups = Set.of("group-13", "group-14", "group-15");
        val user = appUserService.findByEmail("mail@test.pl").get();

        groupService.updateUsersGroups(user.getId(), "instytucja-12", groups);

        assertThat(groupService.getMembers("instytucja-12", "group-13"), hasSize(1));
        assertThat(groupService.getMembers("instytucja-12", "group-14"), hasSize(1));
        assertThat(groupService.getMembers("instytucja-12", "group-15"), hasSize(1));
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

        assertThat(groupService.getMembers("instytucja-12", "group-13"), empty());
        assertThat(groupService.getMembers("instytucja-12", "group-14"), empty());
        assertThat(groupService.getMembers("instytucja-12", "group-15"), empty());

        Group group13 = groupService.save(Group.builder().name("Group 13").slug("group-13").institution(institution).build());
        group13.addMember(user);
        groupService.update(group13);

        Group group14 = groupService.save(Group.builder().name("Group 14").slug("group-14").institution(institution).build());
        group14.addMember(user);
        groupService.update(group14);

        Group group15 = groupService.save(Group.builder().name("Group 15").slug("group-15").institution(institution).build());
        group15.addMember(user);
        groupService.update(group15);

        assertThat(groupService.getMembers("instytucja-12", "group-13"), hasSize(1));
        assertThat(groupService.getMembers("instytucja-12", "group-14"), hasSize(1));
        assertThat(groupService.getMembers("instytucja-12", "group-15"), hasSize(1));

        Set<String> groups = Set.of("group-13", "group-14");

        groupService.updateUsersGroups(user.getId(), "instytucja-12", groups);

        assertThat(groupService.getMembers("instytucja-12", "group-13"), hasSize(1));
        assertThat(groupService.getMembers("instytucja-12", "group-14"), hasSize(1));
        assertThat(groupService.getMembers("instytucja-12", "group-15"), empty());
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
        group13.addMember(user);
        group14.addMember(user);
        group15.addMember(user);
        groupService.update(group13);
        groupService.update(group14);
        groupService.update(group15);

        assertThat(groupService.getMembers("instytucja-12", "group-13"), hasSize(1));
        assertThat(groupService.getMembers("instytucja-12", "group-14"), hasSize(1));
        assertThat(groupService.getMembers("instytucja-12", "group-15"), hasSize(1));

        groupService.updateUsersGroups(user.getId(), "instytucja-12", Set.of());

        assertThat(groupService.getMembers("instytucja-12", "group-13"), empty());
        assertThat(groupService.getMembers("instytucja-12", "group-14"), empty());
        assertThat(groupService.getMembers("instytucja-12", "group-15"), empty());
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

        assertThat(groupService.getMembers("instytucja-12", "group-13"), empty());

        Group group13 = groupService.save(Group.builder().name("Group 13").slug("group-13").institution(institution).build());
        group13.addMember(user);
        groupService.update(group13);

        assertThat(groupService.getMembers("instytucja-12", "group-13"), hasSize(1));

        Group group21 = groupService.save(Group.builder().name("Group 21").slug("group-21").institution(institution2).build());
        group21.addMember(user);
        groupService.update(group21);

        assertThat(groupService.getMembers("instytucja-22", "group-21"), hasSize(1));

        groupService.updateUsersGroups(user.getId(), "instytucja-12", Set.of());

        assertThat(groupService.getMembers("instytucja-12", "group-13"), empty());
        assertThat(groupService.getMembers("instytucja-22", "group-21"), hasSize(1));
    }
}
