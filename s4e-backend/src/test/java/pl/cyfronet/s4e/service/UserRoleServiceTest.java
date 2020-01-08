package pl.cyfronet.s4e.service;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import pl.cyfronet.s4e.BasicTest;
import pl.cyfronet.s4e.TestDbHelper;
import pl.cyfronet.s4e.bean.AppRole;
import pl.cyfronet.s4e.bean.AppUser;
import pl.cyfronet.s4e.bean.Group;
import pl.cyfronet.s4e.bean.Institution;
import pl.cyfronet.s4e.data.repository.AppUserRepository;
import pl.cyfronet.s4e.data.repository.GroupRepository;
import pl.cyfronet.s4e.data.repository.InstitutionRepository;
import pl.cyfronet.s4e.data.repository.UserRoleRepository;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;

@BasicTest
@Slf4j
public class UserRoleServiceTest {
    public static final String PROFILE_EMAIL = "get@profile.com";
    @Autowired
    private UserRoleService userRoleService;

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private UserRoleRepository userRoleRepository;

    @Autowired
    private InstitutionRepository institutionRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private TestDbHelper testDbHelper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private SlugService slugService;

    private String slugInstitution;

    @BeforeEach
    public void setUp() {
        reset();
        appUserRepository.save(AppUser.builder()
                .email(PROFILE_EMAIL)
                .name("Get")
                .surname("Profile")
                .password(passwordEncoder.encode("password"))
                .enabled(true)
                .build());

        String test_institution = "Test Institution";
        slugInstitution = slugService.slugify(test_institution);
        Institution institution = institutionRepository.save(Institution.builder()
                .name(test_institution)
                .slug(slugInstitution)
                .build());
        Group group = groupRepository.save(Group.builder().name("__default__").slug("default").institution(institution).build());
    }

    @AfterEach
    void tearDown() {
        reset();
    }

    private void reset() {
        testDbHelper.clean();
    }

    @Test
    public void shouldAddMemberRole() throws Exception {
        assertThat(userRoleRepository.findUserRolesInInstitution(PROFILE_EMAIL, slugInstitution), hasSize(0));
        userRoleService.addRole(AppRole.GROUP_MEMBER, PROFILE_EMAIL, slugInstitution, "default");
        assertThat(userRoleRepository.findUserRolesInInstitution(PROFILE_EMAIL, slugInstitution), hasSize(1));
    }

    @Test
    public void shouldAddManagerRoleWithMemberRole() throws Exception {
        assertThat(userRoleRepository.findUserRolesInInstitution(PROFILE_EMAIL, slugInstitution), hasSize(0));
        userRoleService.addRole(AppRole.INST_MANAGER, PROFILE_EMAIL, slugInstitution, "default");
        assertThat(userRoleRepository.findUserRolesInInstitution(PROFILE_EMAIL, slugInstitution), hasSize(2));
    }

    @Test
    public void shouldDeleteOnlyManagerRole() throws Exception {
        assertThat(userRoleRepository.findUserRolesInInstitution(PROFILE_EMAIL, slugInstitution), hasSize(0));
        userRoleService.addRole(AppRole.GROUP_MANAGER, PROFILE_EMAIL, slugInstitution, "default");
        assertThat(userRoleRepository.findUserRolesInInstitution(PROFILE_EMAIL, slugInstitution), hasSize(2));
        userRoleService.removeRole(AppRole.GROUP_MANAGER, PROFILE_EMAIL, slugInstitution, "default");
        assertThat(userRoleRepository.findUserRolesInInstitution(PROFILE_EMAIL, slugInstitution), hasSize(1));
    }

    @Test
    public void shouldDeleteMemberAndManagerRole() throws Exception {
        assertThat(userRoleRepository.findUserRolesInInstitution(PROFILE_EMAIL, slugInstitution), hasSize(0));
        userRoleService.addRole(AppRole.GROUP_MANAGER, PROFILE_EMAIL, slugInstitution, "default");
        assertThat(userRoleRepository.findUserRolesInInstitution(PROFILE_EMAIL, slugInstitution), hasSize(2));
        userRoleService.removeRole(AppRole.GROUP_MEMBER, PROFILE_EMAIL, slugInstitution, "default");
        assertThat(userRoleRepository.findUserRolesInInstitution(PROFILE_EMAIL, slugInstitution), hasSize(0));
    }
}
