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

import java.util.HashSet;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

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

        val groupDB = groupService.getGroup("instytucja-12", "group-13");
        val group2DB = groupService.getGroup("instytucja-12", "group-14");
        val institutionDB = institutionService.getInstitution("instytucja-12");

        assertThat(groupDB.isEmpty(), is(true));
        assertThat(group2DB.isPresent(), is(true));
        assertThat(institutionDB.isPresent(), is(true));
    }

    @Test
    public void shouldDeleteGroupAndMembers() throws InstitutionCreationException, GroupCreationException, AppUserCreationException, GroupUpdateException {
        Institution institution = Institution.builder().name("Instytycja 12").slug("instytucja-12").build();
        institutionService.save(institution);

        Group group = groupService.save(Group.builder().name("Group 13").slug("group-13").institution(institution).members(new HashSet<>()).build());

        AppUser user = appUserService.save(AppUser.builder()
                .email("mail@test.pl")
                .password("someHash")
                .build());
        group.addMember(user);
        groupService.update(group);

        val groupDB =  groupService.getGroup("instytucja-12", "group-13");
        val institutionDB = institutionRepository.findBySlug("instytucja-12");
        val appUserDB = appUserService.findByEmail("mail@test.pl");
        Set<AppUser> members = groupService.getMembers("instytucja-12", "group-13");

        assertThat(members.isEmpty(), is(false));
        assertThat(groupDB.isPresent(), is(true));
        assertThat(institutionDB.isPresent(), is(true));
        assertThat(appUserDB.isPresent(), is(true));

        groupService.delete(groupDB.get());
        val appUserDB2 = appUserService.findByEmail("mail@test.pl");
        val groupDB2 =  groupService.getGroup("instytucja-12", "group-13");
        Set<AppUser> members2 = groupService.getMembers("instytucja-12", "group-13");

        assertThat(groupDB2.isEmpty(), is(true));
        assertThat(members2.isEmpty(), is(true));
        assertThat(appUserDB2.isEmpty(), is(false));
        assertThat(institutionDB.isPresent(), is(true));
    }


}
