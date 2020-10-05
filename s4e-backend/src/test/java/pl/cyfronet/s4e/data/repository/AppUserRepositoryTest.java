package pl.cyfronet.s4e.data.repository;

import lombok.val;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import pl.cyfronet.s4e.BasicTest;
import pl.cyfronet.s4e.TestDbHelper;
import pl.cyfronet.s4e.bean.*;
import pl.cyfronet.s4e.service.SlugService;

import static com.github.npathai.hamcrestopt.OptionalMatchers.isPresent;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;

@BasicTest
public class AppUserRepositoryTest {
    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private UserRoleRepository userRoleRepository;

    @Autowired
    private InstitutionRepository institutionRepository;

    private String slugInstitution;
    private String email;

    @Autowired
    private SlugService slugService;

    @Autowired
    private TestDbHelper testDbHelper;

    @BeforeEach
    @Transactional
    void setUp() {
        reset();
        email = "email.@email.com";
        String test_institution = "Test Institution";
        slugInstitution = slugService.slugify(test_institution);
        Institution institution = institutionRepository.save(Institution.builder()
                .name(test_institution)
                .slug(slugInstitution)
                .build());
        AppUser appUser = appUserRepository.save(AppUser.builder()
                .name("Name")
                .surname("Surname")
                .email(email)
                .password("admin123")
                .enabled(true)
                .build());
        userRoleRepository.save(UserRole.builder()
                .role(AppRole.INST_MEMBER)
                .user(appUser)
                .institution(institution)
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
    void shouldFindByEmailWithRolesTest() {
        val dbUser = appUserRepository.findByEmailWithRolesAndInstitutions(email);
        assertThat(dbUser, isPresent());
        assertThat(dbUser.get().getRoles(), hasSize(1));
    }
}
