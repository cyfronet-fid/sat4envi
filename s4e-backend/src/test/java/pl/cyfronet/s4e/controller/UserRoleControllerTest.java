package pl.cyfronet.s4e.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import pl.cyfronet.s4e.BasicTest;
import pl.cyfronet.s4e.TestDbHelper;
import pl.cyfronet.s4e.bean.AppRole;
import pl.cyfronet.s4e.bean.AppUser;
import pl.cyfronet.s4e.bean.Group;
import pl.cyfronet.s4e.bean.Institution;
import pl.cyfronet.s4e.controller.request.CreateUserRoleRequest;
import pl.cyfronet.s4e.controller.request.DeleteUserRoleRequest;
import pl.cyfronet.s4e.data.repository.AppUserRepository;
import pl.cyfronet.s4e.data.repository.GroupRepository;
import pl.cyfronet.s4e.data.repository.InstitutionRepository;
import pl.cyfronet.s4e.data.repository.UserRoleRepository;
import pl.cyfronet.s4e.service.SlugService;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static pl.cyfronet.s4e.Constants.API_PREFIX_V1;
import static pl.cyfronet.s4e.TestJwtUtil.jwtBearerToken;

@BasicTest
@Slf4j
@AutoConfigureMockMvc
public class UserRoleControllerTest {
    public static final String PROFILE_EMAIL = "get@profile.com";
    @Autowired
    private TestDbHelper testDbHelper;

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private UserRoleRepository userRoleRepository;

    @Autowired
    private InstitutionRepository institutionRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SlugService slugService;

    @Autowired
    private MockMvc mockMvc;

    private AppUser appUser;

    private String slugInstitution;

    @BeforeEach
    public void beforeEach() {
        reset();

        appUser = appUserRepository.save(AppUser.builder()
                .email(PROFILE_EMAIL)
                .name("Get")
                .surname("Profile")
                .password("{noop}password")
                .enabled(true)
                .admin(true)
                .build());

        String test_institution = "Test Institution";
        slugInstitution = slugService.slugify(test_institution);
        Institution institution = institutionRepository.save(Institution.builder()
                .name(test_institution)
                .slug(slugInstitution)
                .build());

        Group group = groupRepository.save(Group.builder()
                .name("default")
                .slug("default")
                .institution(institution)
                .build());
    }

    @AfterEach
    public void afterEach() {
        reset();
    }

    private void reset() {
        testDbHelper.clean();
    }

    @Test
    public void shouldAddUserRoleAndThenDelete() throws Exception {
        CreateUserRoleRequest userRoleRequest = CreateUserRoleRequest.builder()
                .email(PROFILE_EMAIL)
                .groupSlug("default")
                .institutionSlug(slugInstitution)
                .role(AppRole.INST_ADMIN)
                .build();

        mockMvc.perform(post(API_PREFIX_V1 + "/user-role")
                .contentType(MediaType.APPLICATION_JSON)
                .with(jwtBearerToken(appUser, objectMapper))
                .content(objectMapper.writeValueAsBytes(userRoleRequest)))
                .andExpect(status().isOk());

        DeleteUserRoleRequest userRoleDeleteRequest = DeleteUserRoleRequest.builder()
                .email(PROFILE_EMAIL)
                .groupSlug("default")
                .institutionSlug(slugInstitution)
                .role(AppRole.INST_ADMIN)
                .build();

        mockMvc.perform(delete(API_PREFIX_V1 + "/user-role")
                .contentType(MediaType.APPLICATION_JSON)
                .with(jwtBearerToken(appUser, objectMapper))
                .content(objectMapper.writeValueAsBytes(userRoleDeleteRequest)))
                .andExpect(status().isOk());
    }
}
