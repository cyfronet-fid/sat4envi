package pl.cyfronet.s4e.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import pl.cyfronet.s4e.BasicTest;
import pl.cyfronet.s4e.bean.AppUser;
import pl.cyfronet.s4e.bean.Institution;
import pl.cyfronet.s4e.controller.request.CreateGroupRequest;
import pl.cyfronet.s4e.controller.request.UpdateGroupRequest;
import pl.cyfronet.s4e.data.repository.AppUserRepository;
import pl.cyfronet.s4e.data.repository.GroupRepository;
import pl.cyfronet.s4e.data.repository.InstitutionRepository;
import pl.cyfronet.s4e.service.GroupService;
import pl.cyfronet.s4e.service.SlugService;

import java.util.HashSet;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.SharedHttpSessionConfigurer.sharedHttpSession;
import static pl.cyfronet.s4e.Constants.API_PREFIX_V1;

@BasicTest
@Slf4j
public class GroupControllerTest {

    @Autowired
    private InstitutionRepository institutionRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private GroupService groupService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SlugService slugService;

    private String slugInstitution = "";

    public static final String PROFILE_EMAIL = "get@profile.com";

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    public void beforeEach() {
        institutionRepository.deleteAll();
        appUserRepository.deleteAll();

        appUserRepository.save(AppUser.builder()
                .email(PROFILE_EMAIL)
                .name("Get")
                .surname("Profile")
                .password(passwordEncoder.encode("password"))
                .enabled(true)
                .build());

        String test_institution = "Test Institution";
        slugInstitution = slugService.slugify(test_institution);
        institutionRepository.save(Institution.builder()
                .name(test_institution)
                .slug(slugInstitution)
                .build());

        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .apply(sharedHttpSession())
                .build();
    }

    @Test
    @WithMockUser
    public void shouldCreateGroupWithoutMembers() throws Exception {
        CreateGroupRequest groupRequest = CreateGroupRequest.builder()
                .name("CreateGroupTest")
                .build();

        mockMvc.perform(post(API_PREFIX_V1 + "/institutions/{institution}/groups", slugInstitution)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(groupRequest)))
                .andExpect(status().isOk());

        assertThat(groupRepository.findByInstitution_SlugAndSlug(slugInstitution, "creategrouptest").isPresent(), is(true));
    }

    @Test
    @WithMockUser
    public void shouldCreateGroupWithMembers() throws Exception {
        Set<String> members = new HashSet<>();
        members.add(PROFILE_EMAIL);
        CreateGroupRequest groupRequest = CreateGroupRequest.builder()
                .name("CreateGroupTest")
                .membersEmails(members)
                .build();

        mockMvc.perform(post(API_PREFIX_V1 + "/institutions/{institution}/groups", slugInstitution)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(groupRequest)))
                .andExpect(status().isOk());

        assertThat(groupRepository.findByInstitution_SlugAndSlug(
                slugInstitution, "creategrouptest").isPresent(), is(true));
        assertThat(groupService.getMembers(slugInstitution, "creategrouptest"), hasSize(1));
    }

    @Test
    @WithMockUser
    public void shouldUpdateGroupWithoutMembers() throws Exception {
        Set<String> members = new HashSet<>();
        members.add(PROFILE_EMAIL);
        CreateGroupRequest groupRequest = CreateGroupRequest.builder()
                .name("CreateGroupTest")
                .membersEmails(members)
                .build();

        mockMvc.perform(post(API_PREFIX_V1 + "/institutions/{institution}/groups", slugInstitution)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(groupRequest)))
                .andExpect(status().isOk());

        assertThat(groupRepository.findByInstitution_SlugAndSlug(
                slugInstitution, "creategrouptest").isPresent(), is(true));
        assertThat(groupService.getMembers(slugInstitution, "creategrouptest"), hasSize(1));

        UpdateGroupRequest groupUpdateRequest = UpdateGroupRequest.builder()
                .name("UpdateGroupTest")
                .build();

        mockMvc.perform(put(API_PREFIX_V1 + "/institutions/{institution}/groups/{group}", slugInstitution, "creategrouptest")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(groupUpdateRequest)))
                .andExpect(status().isOk());

        assertThat(groupRepository.findByInstitution_SlugAndSlug(
                slugInstitution, "updategrouptest").isPresent(), is(true));
        assertThat(groupService.getMembers(slugInstitution, "updategrouptest"), hasSize(1));
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
        Set<String> members = new HashSet<>();
        members.add(PROFILE_EMAIL);
        members.add(email);
        CreateGroupRequest groupRequest = CreateGroupRequest.builder()
                .name("CreateGroupTest")
                .membersEmails(members)
                .build();

        mockMvc.perform(post(API_PREFIX_V1 + "/institutions/{institution}/groups", slugInstitution)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(groupRequest)))
                .andExpect(status().isOk());

        assertThat(groupRepository.findByInstitution_SlugAndSlug(
                slugInstitution, "creategrouptest").isPresent(), is(true));
        assertThat(groupService.getMembers(slugInstitution, "creategrouptest"), hasSize(2));

        members.remove(email);
        UpdateGroupRequest groupUpdateRequest = UpdateGroupRequest.builder()
                .name("UpdateGroupTest")
                .membersEmails(members)
                .build();

        mockMvc.perform(put(API_PREFIX_V1 + "/institutions/{institution}/groups/{group}", slugInstitution, "creategrouptest")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(groupUpdateRequest)))
                .andExpect(status().isOk());

        assertThat(groupRepository.findByInstitution_SlugAndSlug(
                slugInstitution, "updategrouptest").isPresent(), is(true));
        assertThat(groupService.getMembers(slugInstitution, "updategrouptest"), hasSize(1));
    }

    @Test
    @WithMockUser
    public void shouldNotCreateGroupWithoutMembersIfNameInvalid() throws Exception {
        CreateGroupRequest groupRequest = CreateGroupRequest.builder()
                .name("__default__")
                .build();

        mockMvc.perform(post(API_PREFIX_V1 + "/institutions/{institution}/groups", slugInstitution)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(groupRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.name[0]", containsString("nie może zawierać podkreśleń")));
    }
}
