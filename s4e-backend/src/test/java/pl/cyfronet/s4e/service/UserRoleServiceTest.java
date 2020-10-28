package pl.cyfronet.s4e.service;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
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

    private String institutionSlug;
    private Long userId;

    @BeforeEach
    public void setUp() {
        reset();
        val appUser = appUserRepository.save(AppUser.builder()
                .email(PROFILE_EMAIL)
                .name("Get")
                .surname("Profile")
                .password("{noop}password")
                .enabled(true)
                .build());
        userId = appUser.getId();

        String test_institution = "Test Institution";
        institutionSlug = slugService.slugify(test_institution);
        institutionRepository.save(Institution.builder()
                .name(test_institution)
                .slug(institutionSlug)
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
        assertThat(userRoleRepository.findUserRolesInInstitution(PROFILE_EMAIL, institutionSlug), hasSize(0));
        userRoleService.addRole(institutionSlug, userId, AppRole.INST_MEMBER);
        assertThat(userRoleRepository.findUserRolesInInstitution(PROFILE_EMAIL, institutionSlug), hasSize(1));
    }

    @Test
    public void shouldDeleteOnlyAdminRole() throws Exception {
        assertThat(userRoleRepository.findUserRolesInInstitution(PROFILE_EMAIL, institutionSlug), hasSize(0));
        userRoleService.addRole(institutionSlug, userId, AppRole.INST_ADMIN);
        assertThat(userRoleRepository.findUserRolesInInstitution(PROFILE_EMAIL, institutionSlug), hasSize(2));
        userRoleService.removeRole(institutionSlug, userId, AppRole.INST_ADMIN);
        assertThat(userRoleRepository.findUserRolesInInstitution(PROFILE_EMAIL, institutionSlug), hasSize(1));
    }

    @Test
    public void shouldDeleteMemberAndAdminRole() throws Exception {
        assertThat(userRoleRepository.findUserRolesInInstitution(PROFILE_EMAIL, institutionSlug), hasSize(0));
        userRoleService.addRole(institutionSlug, userId, AppRole.INST_ADMIN);
        assertThat(userRoleRepository.findUserRolesInInstitution(PROFILE_EMAIL, institutionSlug), hasSize(2));
        userRoleService.removeRole(institutionSlug, userId, AppRole.INST_MEMBER);
        assertThat(userRoleRepository.findUserRolesInInstitution(PROFILE_EMAIL, institutionSlug), hasSize(0));
    }
}
