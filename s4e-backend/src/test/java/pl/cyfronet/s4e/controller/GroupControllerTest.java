package pl.cyfronet.s4e.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static pl.cyfronet.s4e.Constants.API_PREFIX_V1;

@AutoConfigureMockMvc
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

    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    public void beforeEach() {
        institutionRepository.deleteAll();
        appUserRepository.deleteAll();

        String test_institution = "Test Institution";
        slugInstitution = slugService.slugify(test_institution);
        institutionRepository.save(Institution.builder()
                .name(test_institution)
                .slug(slugInstitution)
                .build());
    }

    @Test
    public void shouldCreateGroupWithoutMembers() throws Exception {
        CreateGroupRequest groupRequest = CreateGroupRequest.builder()
                .name("CreateGroupTest")
                .build();

        mockMvc.perform(post(API_PREFIX_V1+"/institutions/{institution}/groups", slugInstitution)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(groupRequest)))
                .andExpect(status().isOk());

        assertThat(groupRepository.findBySlugAndInstitution_Slug("creategrouptest",slugInstitution).isPresent(), is(true));
    }

    @Test
    public void shouldCreateGroupWithMembers() throws Exception {
        String email = "some@email.pl";
        appUserRepository.save(AppUser.builder()
                .name("Name")
                .surname("Surname")
                .email(email)
                .password("admin123")
                .enabled(true)
                .build());
        Set<String> members = new HashSet<>();
        members.add(email);
        CreateGroupRequest groupRequest = CreateGroupRequest.builder()
                .name("CreateGroupTest")
                .membersEmails(members)
                .build();

        mockMvc.perform(post(API_PREFIX_V1+"/institutions/{institution}/groups", slugInstitution)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(groupRequest)))
                .andExpect(status().isOk());

        assertThat(groupRepository.findBySlugAndInstitution_Slug(
                "creategrouptest",slugInstitution).isPresent(), is(true));
        assertThat(groupService.getMembers(slugInstitution,"creategrouptest"), hasSize(1));
    }

    @Test
    public void shouldUpdateGroupWithoutMembers() throws Exception {
        String email = "some@email.pl";
        appUserRepository.save(AppUser.builder()
                .name("Name")
                .surname("Surname")
                .email(email)
                .password("admin123")
                .enabled(true)
                .build());
        Set<String> members = new HashSet<>();
        members.add(email);
        CreateGroupRequest groupRequest = CreateGroupRequest.builder()
                .name("CreateGroupTest")
                .membersEmails(members)
                .build();

        mockMvc.perform(post(API_PREFIX_V1+"/institutions/{institution}/groups", slugInstitution)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(groupRequest)))
                .andExpect(status().isOk());

        assertThat(groupRepository.findBySlugAndInstitution_Slug(
                "creategrouptest",slugInstitution).isPresent(), is(true));
        assertThat(groupService.getMembers(slugInstitution,"creategrouptest"), hasSize(1));

        UpdateGroupRequest groupUpdateRequest = UpdateGroupRequest.builder()
                .name("UpdateGroupTest")
                .build();

        mockMvc.perform(put(API_PREFIX_V1+"/institutions/{institution}/groups/{group}", slugInstitution,"creategrouptest")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(groupUpdateRequest)))
                .andExpect(status().isOk());

        assertThat(groupRepository.findBySlugAndInstitution_Slug("updategrouptest",slugInstitution).isPresent(), is(true));
        assertThat(groupService.getMembers(slugInstitution,"updategrouptest"), hasSize(1));
    }

    @Test
    public void shouldUpdateGroupWithMembers() throws Exception {
        String email = "some@email.pl";
        String email2 = "some2@email.pl";
        appUserRepository.save(AppUser.builder()
                .name("Name")
                .surname("Surname")
                .email(email)
                .password("admin123")
                .enabled(true)
                .build());
        appUserRepository.save(AppUser.builder()
                .name("Name2")
                .surname("Surname2")
                .email(email2)
                .password("admin123")
                .enabled(true)
                .build());
        Set<String> members = new HashSet<>();
        members.add(email);
        members.add(email2);
        CreateGroupRequest groupRequest = CreateGroupRequest.builder()
                .name("CreateGroupTest")
                .membersEmails(members)
                .build();

        mockMvc.perform(post(API_PREFIX_V1+"/institutions/{institution}/groups", slugInstitution)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(groupRequest)))
                .andExpect(status().isOk());

        assertThat(groupRepository.findBySlugAndInstitution_Slug(
                "creategrouptest",slugInstitution).isPresent(), is(true));
        assertThat(groupService.getMembers(slugInstitution,"creategrouptest"), hasSize(2));

        members.remove(email);
        UpdateGroupRequest groupUpdateRequest = UpdateGroupRequest.builder()
                .name("UpdateGroupTest")
                .membersEmails(members)
                .build();

        mockMvc.perform(put(API_PREFIX_V1+"/institutions/{institution}/groups/{group}", slugInstitution,"creategrouptest")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(groupUpdateRequest)))
                .andExpect(status().isOk());

        assertThat(groupRepository.findBySlugAndInstitution_Slug("updategrouptest",slugInstitution).isPresent(), is(true));
        assertThat(groupService.getMembers(slugInstitution,"updategrouptest"), hasSize(1));
    }
}
