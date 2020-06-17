package pl.cyfronet.s4e.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import pl.cyfronet.s4e.BasicTest;
import pl.cyfronet.s4e.TestDbHelper;
import pl.cyfronet.s4e.bean.*;
import pl.cyfronet.s4e.data.repository.AppUserRepository;
import pl.cyfronet.s4e.data.repository.GroupRepository;
import pl.cyfronet.s4e.data.repository.InstitutionRepository;
import pl.cyfronet.s4e.data.repository.UserRoleRepository;
import pl.cyfronet.s4e.service.SlugService;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static pl.cyfronet.s4e.Constants.API_PREFIX_V1;
import static pl.cyfronet.s4e.TestJwtUtil.jwtBearerToken;

@BasicTest
@AutoConfigureMockMvc
public class InstitutionControllerTest {
    public static final String PROFILE_EMAIL = "get@profile.com";

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private InstitutionRepository institutionRepository;

    @Autowired
    private UserRoleRepository userRoleRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TestDbHelper testDbHelper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private SlugService slugService;

    @Autowired
    private MockMvc mockMvc;

    private AppUser appUser;

    private String testInstitution = "Test Institution";
    private String slugInstitution;

    @BeforeEach
    public void beforeEach() {
        testDbHelper.clean();
        appUser = appUserRepository.save(AppUser.builder()
                .email(PROFILE_EMAIL)
                .name("Get")
                .surname("Profile")
                .password(passwordEncoder.encode("password"))
                .enabled(true)
                .build());

        slugInstitution = slugService.slugify(testInstitution);
        Institution institution = institutionRepository.save(Institution.builder()
                .name(testInstitution)
                .slug(slugInstitution)
                .build());
        Group group = groupRepository.save(Group.builder().name("__default__").slug("default").institution(institution).build());

        UserRole userRole = UserRole.builder().role(AppRole.GROUP_MEMBER).user(appUser).group(group).build();
        userRoleRepository.save(userRole);
    }

    @AfterEach
    public void afterEach() {
        testDbHelper.clean();
    }

    @Test
    public void shouldGetUserInstitution() throws Exception {
        mockMvc.perform(get(API_PREFIX_V1 + "/institutions")
                .contentType(MediaType.APPLICATION_JSON)
                .with(jwtBearerToken(appUser, objectMapper)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value(is(equalTo(testInstitution))))
                .andExpect(jsonPath("$[0].slug").value(is(equalTo(slugInstitution))));
    }

    @Test
    public void shouldGetEmptySet() throws Exception {
        AppUser appUser = appUserRepository.save(AppUser.builder()
                .email("get2@profile.com")
                .name("Get")
                .surname("Profile")
                .password(passwordEncoder.encode("password"))
                .enabled(true)
                .build());

        mockMvc.perform(get(API_PREFIX_V1 + "/institutions")
                .contentType(MediaType.APPLICATION_JSON)
                .with(jwtBearerToken(appUser, objectMapper)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(equalTo(0))));
    }

    @Test
    public void shouldGetAllUsersInstitutions() throws Exception {
        String testInstitutionZK = "Test Institution ZK";
        String slugInstitutionZK = slugService.slugify(testInstitutionZK);
        Institution institution = institutionRepository.save(Institution.builder()
                .name(testInstitutionZK)
                .slug(slugInstitutionZK)
                .build());
        Group group = groupRepository.save(Group.builder().name("__default__").slug("default").institution(institution).build());

        UserRole userRole = UserRole.builder().role(AppRole.GROUP_MEMBER).user(appUser).group(group).build();
        userRoleRepository.save(userRole);

        String testInstitutionZK_MAZ = "Test Institution ZK - Mazowieckie";
        String slugInstitutionZK_MAZ = slugService.slugify(testInstitutionZK_MAZ);
        Institution institution2 = institutionRepository.save(Institution.builder()
                .name(testInstitutionZK_MAZ)
                .slug(slugInstitutionZK_MAZ)
                .parent(institution)
                .build());
        Group group2 = groupRepository.save(Group.builder().name("__default__").slug("default").institution(institution2).build());

        userRole = UserRole.builder().role(AppRole.GROUP_MEMBER).user(appUser).group(group2).build();
        userRoleRepository.save(userRole);

        String testInstitutionPAK = "Test Institution PAK";
        String slugInstitutionPAK = slugService.slugify(testInstitutionPAK);
        institution = institutionRepository.save(Institution.builder()
                .name(testInstitutionPAK)
                .slug(slugInstitutionPAK)
                .build());
        groupRepository.save(Group.builder().name("__default__").slug("default").institution(institution).build());

        mockMvc.perform(get(API_PREFIX_V1 + "/institutions")
                .contentType(MediaType.APPLICATION_JSON)
                .with(jwtBearerToken(appUser, objectMapper)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(equalTo(3))));
    }

    @Test
    public void shouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get(API_PREFIX_V1 + "/institutions")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }
}
