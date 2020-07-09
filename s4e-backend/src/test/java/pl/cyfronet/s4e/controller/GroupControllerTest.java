package pl.cyfronet.s4e.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import pl.cyfronet.s4e.BasicTest;
import pl.cyfronet.s4e.bean.*;
import pl.cyfronet.s4e.controller.request.CreateGroupRequest;
import pl.cyfronet.s4e.controller.request.UpdateGroupRequest;
import pl.cyfronet.s4e.controller.response.AppUserResponse;
import pl.cyfronet.s4e.data.repository.AppUserRepository;
import pl.cyfronet.s4e.data.repository.GroupRepository;
import pl.cyfronet.s4e.data.repository.InstitutionRepository;
import pl.cyfronet.s4e.data.repository.UserRoleRepository;
import pl.cyfronet.s4e.service.GroupService;
import pl.cyfronet.s4e.service.SlugService;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static com.github.npathai.hamcrestopt.OptionalMatchers.isPresent;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static pl.cyfronet.s4e.Constants.API_PREFIX_V1;
import static pl.cyfronet.s4e.TestJwtUtil.jwtBearerToken;

@BasicTest
@Slf4j
@AutoConfigureMockMvc
public class GroupControllerTest {
    public static final String PROFILE_EMAIL = "get@profile.com";

    @Autowired
    private InstitutionRepository institutionRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private UserRoleRepository userRoleRepository;

    @Autowired
    private GroupService groupService;

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
        institutionRepository.deleteAll();
        appUserRepository.deleteAll();

        appUser = appUserRepository.save(AppUser.builder()
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
        Group group = groupRepository.save(Group.builder().name("__default__").slug("default").institution(institution).build());

        UserRole userRole = UserRole.builder().role(AppRole.INST_MANAGER).user(appUser).group(group).build();
        userRoleRepository.save(userRole);
    }

    @Test
    public void shouldCreateGroupWithoutMembers() throws Exception {
        CreateGroupRequest groupRequest = CreateGroupRequest.builder()
                .name("CreateGroupTest")
                .build();

        mockMvc.perform(post(API_PREFIX_V1 + "/institutions/{institution}/groups", slugInstitution)
                .contentType(MediaType.APPLICATION_JSON)
                .with(jwtBearerToken(appUser, objectMapper))
                .content(objectMapper.writeValueAsBytes(groupRequest)))
                .andExpect(status().isOk());

        assertThat(groupRepository.findByInstitution_SlugAndSlug(slugInstitution, "creategrouptest", Group.class), isPresent());
    }

    @Test
    @WithMockUser
    public void shouldCreateGroupWithMembers() throws Exception {
        Map<String, Set<AppRole>> membersRoles = new HashMap<>();
        membersRoles.put(PROFILE_EMAIL, Set.of(AppRole.GROUP_MEMBER));
        CreateGroupRequest groupRequest = CreateGroupRequest.builder()
                .name("CreateGroupTest")
                .membersRoles(membersRoles)
                .build();

        mockMvc.perform(post(API_PREFIX_V1 + "/institutions/{institution}/groups", slugInstitution)
                .contentType(MediaType.APPLICATION_JSON)
                .with(jwtBearerToken(appUser, objectMapper))
                .content(objectMapper.writeValueAsBytes(groupRequest)))
                .andExpect(status().isOk());

        assertThat(groupRepository.findByInstitution_SlugAndSlug(
                slugInstitution, "creategrouptest", Group.class), isPresent());
        assertThat(groupService.getMembers(slugInstitution, "creategrouptest", AppUserResponse.class), hasSize(1));
    }

    @Test
    public void shouldUpdateGroupWithoutMembers() throws Exception {
        Map<String, Set<AppRole>> membersRoles = new HashMap<>();
        membersRoles.put(PROFILE_EMAIL, Set.of(AppRole.GROUP_MEMBER));
        Set<String> members = new HashSet<>();
        members.add(PROFILE_EMAIL);
        CreateGroupRequest groupRequest = CreateGroupRequest.builder()
                .name("CreateGroupTest")
                .membersRoles(membersRoles)
                .build();

        mockMvc.perform(post(API_PREFIX_V1 + "/institutions/{institution}/groups", slugInstitution)
                .contentType(MediaType.APPLICATION_JSON)
                .with(jwtBearerToken(appUser, objectMapper))
                .content(objectMapper.writeValueAsBytes(groupRequest)))
                .andExpect(status().isOk());

        assertThat(groupRepository.findByInstitution_SlugAndSlug(
                slugInstitution, "creategrouptest", Group.class), isPresent());
        assertThat(groupService.getMembers(slugInstitution, "creategrouptest", AppUserResponse.class), hasSize(1));

        UpdateGroupRequest groupUpdateRequest = UpdateGroupRequest.builder()
                .name("UpdateGroupTest")
                .build();

        mockMvc.perform(put(API_PREFIX_V1 + "/institutions/{institution}/groups/{group}", slugInstitution, "creategrouptest")
                .contentType(MediaType.APPLICATION_JSON)
                .with(jwtBearerToken(appUser, objectMapper))
                .content(objectMapper.writeValueAsBytes(groupUpdateRequest)))
                .andExpect(status().isOk());

        assertThat(groupRepository.findByInstitution_SlugAndSlug(
                slugInstitution, "updategrouptest", Group.class), isPresent());
        assertThat(groupService.getMembers(slugInstitution, "updategrouptest", AppUserResponse.class), hasSize(1));
    }

    @Test
    @WithMockUser
    public void shouldUpdateGroupWithMembers() throws Exception {
        String email = "some@email.pl";
        appUserRepository.save(AppUser.builder()
                .name("Name")
                .surname("Surname")
                .email(email)
                .password("admin123")
                .enabled(true)
                .build());
        Map<String, Set<AppRole>> membersRoles = new HashMap<>();
        membersRoles.put(PROFILE_EMAIL, Set.of(AppRole.GROUP_MEMBER));
        membersRoles.put(email, Set.of(AppRole.GROUP_MEMBER));
        CreateGroupRequest groupRequest = CreateGroupRequest.builder()
                .name("CreateGroupTest")
                .membersRoles(membersRoles)
                .build();

        mockMvc.perform(post(API_PREFIX_V1 + "/institutions/{institution}/groups", slugInstitution)
                .contentType(MediaType.APPLICATION_JSON)
                .with(jwtBearerToken(appUser, objectMapper))
                .content(objectMapper.writeValueAsBytes(groupRequest)))
                .andExpect(status().isOk());

        assertThat(groupRepository.findByInstitution_SlugAndSlug(
                slugInstitution, "creategrouptest", Group.class), isPresent());
        assertThat(groupService.getMembers(slugInstitution, "creategrouptest", AppUserResponse.class), hasSize(2));

        membersRoles.remove(email);
        UpdateGroupRequest groupUpdateRequest = UpdateGroupRequest.builder()
                .name("UpdateGroupTest")
                .membersRoles(membersRoles)
                .build();

        mockMvc.perform(put(API_PREFIX_V1 + "/institutions/{institution}/groups/{group}", slugInstitution, "creategrouptest")
                .contentType(MediaType.APPLICATION_JSON)
                .with(jwtBearerToken(appUser, objectMapper))
                .content(objectMapper.writeValueAsBytes(groupUpdateRequest)))
                .andExpect(status().isOk());

        assertThat(groupRepository.findByInstitution_SlugAndSlug(
                slugInstitution, "updategrouptest", Group.class), isPresent());
        assertThat(groupService.getMembers(slugInstitution, "updategrouptest", AppUserResponse.class), hasSize(1));
    }

    @Test
    @WithMockUser
    public void shouldNotCreateGroupWithoutMembersIfNameInvalid() throws Exception {
        CreateGroupRequest groupRequest = CreateGroupRequest.builder()
                .name("__default__")
                .build();

        mockMvc.perform(post(API_PREFIX_V1 + "/institutions/{institution}/groups", slugInstitution)
                .contentType(MediaType.APPLICATION_JSON)
                .with(jwtBearerToken(appUser, objectMapper))
                .content(objectMapper.writeValueAsBytes(groupRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.name[0]", containsString("nie może zawierać podkreśleń")));
    }
}
