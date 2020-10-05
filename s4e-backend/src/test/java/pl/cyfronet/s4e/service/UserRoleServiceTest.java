package pl.cyfronet.s4e.service;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import pl.cyfronet.s4e.BasicTest;
import pl.cyfronet.s4e.TestDbHelper;
import pl.cyfronet.s4e.bean.AppRole;
import pl.cyfronet.s4e.bean.AppUser;
import pl.cyfronet.s4e.bean.Institution;
import pl.cyfronet.s4e.data.repository.AppUserRepository;
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
    private TestDbHelper testDbHelper;

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
                .password("{noop}password")
                .enabled(true)
                .build());

        String test_institution = "Test Institution";
        slugInstitution = slugService.slugify(test_institution);
        Institution institution = institutionRepository.save(Institution.builder()
                .name(test_institution)
                .slug(slugInstitution)
                .build());
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
        userRoleService.addRole(AppRole.INST_MEMBER, PROFILE_EMAIL, slugInstitution);
        assertThat(userRoleRepository.findUserRolesInInstitution(PROFILE_EMAIL, slugInstitution), hasSize(1));
    }

    @Test
    public void shouldDeleteOnlyManagerRole() throws Exception {
        assertThat(userRoleRepository.findUserRolesInInstitution(PROFILE_EMAIL, slugInstitution), hasSize(0));
        userRoleService.addRole(AppRole.INST_ADMIN, PROFILE_EMAIL, slugInstitution);
        assertThat(userRoleRepository.findUserRolesInInstitution(PROFILE_EMAIL, slugInstitution), hasSize(2));
        userRoleService.removeRole(AppRole.INST_ADMIN, PROFILE_EMAIL, slugInstitution);
        assertThat(userRoleRepository.findUserRolesInInstitution(PROFILE_EMAIL, slugInstitution), hasSize(1));
    }

    @Test
    public void shouldDeleteMemberAndManagerRole() throws Exception {
        assertThat(userRoleRepository.findUserRolesInInstitution(PROFILE_EMAIL, slugInstitution), hasSize(0));
        userRoleService.addRole(AppRole.INST_ADMIN, PROFILE_EMAIL, slugInstitution);
        assertThat(userRoleRepository.findUserRolesInInstitution(PROFILE_EMAIL, slugInstitution), hasSize(2));
        userRoleService.removeRole(AppRole.INST_MEMBER, PROFILE_EMAIL, slugInstitution);
        assertThat(userRoleRepository.findUserRolesInInstitution(PROFILE_EMAIL, slugInstitution), hasSize(0));
    }
}
