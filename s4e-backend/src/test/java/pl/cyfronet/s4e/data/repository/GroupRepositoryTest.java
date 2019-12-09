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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;

@BasicTest
public class GroupRepositoryTest {
    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private UserRoleRepository userRoleRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private InstitutionRepository institutionRepository;

    private String slugInstitution;
    private String email;
    private String email2;

    @Autowired
    private SlugService slugService;

    @Autowired
    private TestDbHelper testDbHelper;

    @BeforeEach
    @Transactional
    void setUp() {
        reset();
        email = "email.@email.com";
        email2 = "email2.@email.com";
        String test_institution = "Test Institution";
        slugInstitution = slugService.slugify(test_institution);
        Institution institution = institutionRepository.save(Institution.builder()
                .name(test_institution)
                .slug(slugInstitution)
                .build());
        Group group = groupRepository.save(Group.builder()
                .name("__default")
                .slug("default")
                .institution(institution)
                .build());
        AppUser appUser = appUserRepository.save(AppUser.builder()
                .name("Name")
                .surname("Surname")
                .email(email)
                .password("admin123")
                .enabled(true)
                .build());
        userRoleRepository.save(UserRole.builder()
                .role(AppRole.GROUP_MEMBER)
                .user(appUser)
                .group(group)
                .build());
        AppUser appUser2 = appUserRepository.save(AppUser.builder()
                .name("Name2")
                .surname("Surname2")
                .email(email2)
                .password("admin123")
                .enabled(true)
                .build());
        userRoleRepository.save(UserRole.builder()
                .role(AppRole.GROUP_MEMBER)
                .user(appUser2)
                .group(group)
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
    void shouldFindAllMembersEmails() {
        val membersEmails = groupRepository.findAllMembersEmails(slugInstitution, "default", AppRole.GROUP_MEMBER);
        assertThat(membersEmails, hasSize(2));
    }
}
