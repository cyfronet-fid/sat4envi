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
import pl.cyfronet.s4e.bean.UserRole;
import pl.cyfronet.s4e.controller.request.CreateChildInstitutionRequest;
import pl.cyfronet.s4e.controller.response.AppUserResponse;
import pl.cyfronet.s4e.data.repository.AppUserRepository;
import pl.cyfronet.s4e.data.repository.InstitutionRepository;
import pl.cyfronet.s4e.data.repository.UserRoleRepository;
import pl.cyfronet.s4e.ex.InstitutionCreationException;
import pl.cyfronet.s4e.ex.NotFoundException;

import java.util.Set;

import static com.github.npathai.hamcrestopt.OptionalMatchers.isEmpty;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@BasicTest
@Slf4j
public class InstitutionServiceTest {
    public static final String DEFAULT = "default";
    public static final String PROFILE_EMAIL = "get@profile.com";
    public static final String PROFILE_EMAIL2 = "get2@profile.com";
    public static final String PROFILE_EMAIL3 = "get3@profile.com";
    @Autowired
    private InstitutionService institutionService;
    @Autowired
    private InstitutionRepository institutionRepository;
    @Autowired
    private UserRoleRepository userRoleRepository;
    @Autowired
    private AppUserRepository appUserRepository;
    @Autowired
    private SlugService slugService;
    @Autowired
    private TestDbHelper testDbHelper;
    private String testInstitution = "Test Institution - root";
    private String testInstitution2 = "Test Institution - lvl1";
    private String slugInstitution;
    private String slugInstitution2;
    private AppUser appUser;
    private AppUser appUser2;

    @BeforeEach
    public void setUp() {
        testDbHelper.clean();
        appUser = appUserRepository.save(AppUser.builder()
                .email(PROFILE_EMAIL)
                .name("Get")
                .surname("Profile")
                .password("{noop}password")
                .enabled(true)
                .build());
        appUser2 = appUserRepository.save(AppUser.builder()
                .email(PROFILE_EMAIL2)
                .name("Get2")
                .surname("Profile2")
                .password("{noop}password")
                .enabled(true)
                .build());
        //1st lvl institution
        slugInstitution = slugService.slugify(testInstitution);
        Institution institution = institutionRepository.save(Institution.builder()
                .name(testInstitution)
                .slug(slugInstitution)
                .build());

        UserRole userRole = UserRole.builder().role(AppRole.GROUP_MEMBER).user(appUser).institution(institution).build();
        userRoleRepository.save(userRole);
        userRole = UserRole.builder().role(AppRole.INST_ADMIN).user(appUser).institution(institution).build();
        userRoleRepository.save(userRole);

        // 2nd lvl institution
        slugInstitution2 = slugService.slugify(testInstitution2);
        Institution institution2 = institutionRepository.save(Institution.builder()
                .name(testInstitution2)
                .parent(institution)
                .slug(slugInstitution2)
                .build());

        userRole = UserRole.builder().role(AppRole.GROUP_MEMBER).user(appUser2).institution(institution2).build();
        userRoleRepository.save(userRole);
        userRole = UserRole.builder().role(AppRole.INST_ADMIN).user(appUser2).institution(institution2).build();
        userRoleRepository.save(userRole);
        userRole = UserRole.builder().role(AppRole.GROUP_MEMBER).user(appUser).institution(institution2).build();
        userRoleRepository.save(userRole);
        userRole = UserRole.builder().role(AppRole.INST_ADMIN).user(appUser).institution(institution2).build();
        userRoleRepository.save(userRole);
    }

    @AfterEach
    public void tearDown() {
        testDbHelper.clean();
    }

    @Test
    public void shouldDeleteInstitution() throws InstitutionCreationException {
        Institution institution = Institution.builder().name("Instytycja 15").slug("instytucja-15").build();
        institutionService.save(institution);

        institutionService.delete("instytucja-15");
        val institutionDB = institutionService.findBySlug("instytucja-15", Institution.class);

        assertThat(institutionDB, isEmpty());
    }

    @Test
    public void shouldAddChildInstitutionAndParentInstitutionAdmin() throws NotFoundException, InstitutionCreationException {
        AppUser childAdmin = appUserRepository.save(AppUser.builder()
                .email(PROFILE_EMAIL3)
                .name("Get3")
                .surname("Profile3")
                .password("{noop}password")
                .enabled(true)
                .build());
        CreateChildInstitutionRequest request = CreateChildInstitutionRequest.builder()
                .name("child-institution")
                .institutionAdminEmail(childAdmin.getEmail())
                .build();

        Set<AppUserResponse> result = institutionService.getMembers("child-institution", AppUserResponse.class);
        assertThat(result, hasSize(0));
        institutionService.createChildInstitution(request, slugInstitution2);
        result = institutionService.getMembers("child-institution", AppUserResponse.class);
        // new inst_admin and two from parent
        assertThat(result, hasSize(3));
    }

    @Test
    public void shouldReturnParentSlug() throws NotFoundException {
        assertThat(institutionService.getParentSlugBy(slugInstitution2), is(equalTo(slugInstitution)));
    }

    @Test
    public void shouldntReturnParentSlugForRoot() throws NotFoundException {
        assertThat(institutionService.getParentSlugBy(slugInstitution), nullValue());
    }

    @Test
    public void shouldReturnParentNameByInstitutionSlug() throws NotFoundException {
        assertThat(institutionService.getParentNameBy(slugInstitution2), is(equalTo(testInstitution)));
    }

    @Test
    public void shouldntReturnParentNameForRoot() throws NotFoundException {
        assertThat(institutionService.getParentNameBy(slugInstitution), nullValue());
    }
}
