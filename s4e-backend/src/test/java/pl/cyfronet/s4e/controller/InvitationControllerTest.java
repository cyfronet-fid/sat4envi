package pl.cyfronet.s4e.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import pl.cyfronet.s4e.BasicTest;
import pl.cyfronet.s4e.InvitationHelper;
import pl.cyfronet.s4e.TestDbHelper;
import pl.cyfronet.s4e.bean.*;
import pl.cyfronet.s4e.data.repository.*;
import pl.cyfronet.s4e.service.SlugService;

import static com.github.npathai.hamcrestopt.OptionalMatchers.isEmpty;
import static com.github.npathai.hamcrestopt.OptionalMatchers.isPresent;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static pl.cyfronet.s4e.Constants.API_PREFIX_V1;
import static pl.cyfronet.s4e.TestJwtUtil.jwtBearerToken;

@BasicTest
@AutoConfigureMockMvc
public class InvitationControllerTest {
    @Autowired
    private InstitutionRepository institutionRepository;

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TestDbHelper testDbHelper;

    @Autowired
    private SlugService slugService;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private UserRoleRepository userRoleRepository;

    @Autowired
    private InvitationRepository invitationRepository;

    private AppUser institutionAdmin;
    private AppUser user;
    private AppUser member;
    private Institution institution;

    @BeforeEach
    public void beforeEach() {
        reset();

        institution = institutionRepository.save(InvitationHelper.institutionBuilder().build());
        Group group = groupRepository
                .save(InvitationHelper.defaultGroupBuilder(institution).build());

        institutionAdmin = appUserRepository.save(InvitationHelper.userBuilder().build());
        val institutionAdminRoles = new AppRole[]{AppRole.INST_ADMIN, AppRole.INST_MANAGER, AppRole.GROUP_MEMBER};
        addRoles(institutionAdmin, group, institutionAdminRoles);

        member = appUserRepository.save(InvitationHelper.userBuilder().build());
        val memberRoles = new AppRole[]{AppRole.GROUP_MEMBER};
        addRoles(member, group, memberRoles);

        user = appUserRepository.save(InvitationHelper.userBuilder().build());
    }

    @AfterEach
    public void afterEach() {
        reset();
    }

    @Test
    public void getShouldBeSecured() throws Exception {
        val URL = API_PREFIX_V1 + "/institutions/{institution}/invitations";
        mockMvc.perform(get(URL, institution.getSlug())
                .contentType(MediaType.APPLICATION_JSON)
                .with(jwtBearerToken(member, objectMapper))
        ).andExpect(status().isForbidden());
    }

    @Test
    public void shouldGet() throws Exception {
        val URL = API_PREFIX_V1 + "/institutions/{institution}/invitations";
        mockMvc.perform(get(URL, institution.getSlug())
                .contentType(MediaType.APPLICATION_JSON)
                .with(jwtBearerToken(institutionAdmin, objectMapper))
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(equalTo(0))));

        invitationRepository
                .save(InvitationHelper.invitationBuilder(institution).build());
        mockMvc.perform(get(URL, institution.getSlug())
                .contentType(MediaType.APPLICATION_JSON)
                .with(jwtBearerToken(institutionAdmin, objectMapper))
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(equalTo(1))));
    }

    @Test
    public void createShouldBeSecured() throws Exception {
        val request = InvitationHelper.invitationRequestBuilder().build();
        val URL = API_PREFIX_V1 + "/institutions/{institution}/invitations";
        mockMvc.perform(post(URL, institution.getSlug())
                .contentType(MediaType.APPLICATION_JSON)
                .with(jwtBearerToken(member, objectMapper))
                .content(objectMapper.writeValueAsBytes(request))
        ).andExpect(status().isForbidden());
    }

    @Test
    public void createShouldHandleNFE() throws Exception {
        val institution = InvitationHelper.institutionBuilder().build();
        val request = InvitationHelper.invitationRequestBuilder().build();
        val URL = API_PREFIX_V1 + "/institutions/{institution}/invitations";
        mockMvc.perform(post(URL, institution.getSlug())
                .contentType(MediaType.APPLICATION_JSON)
                .with(jwtBearerToken(institutionAdmin, objectMapper))
                .content(objectMapper.writeValueAsBytes(request))
        ).andExpect(status().isForbidden());
    }

    @Test
    public void shouldCreate() throws Exception {
        val request = InvitationHelper.invitationRequestBuilder().build();
        val URL = API_PREFIX_V1 + "/institutions/{institution}/invitations";
        mockMvc.perform(post(URL, institution.getSlug())
                    .contentType(MediaType.APPLICATION_JSON)
                    .with(jwtBearerToken(institutionAdmin, objectMapper))
                    .content(objectMapper.writeValueAsBytes(request))
        ).andExpect(status().isOk());

        val dbInvitation = invitationRepository
                .findByEmailAndInstitutionSlug(
                        request.getEmail(),
                        institution.getSlug(),
                        Invitation.class
                );
        assertThat(dbInvitation, isPresent());
        assertEquals(dbInvitation.get().getStatus(), InvitationStatus.WAITING);
    }

    @Test
    public void resendShouldBeSecured() throws Exception {
        val request = InvitationHelper.invitationRequestBuilder().build();
        val invitation = invitationRepository
                .save(InvitationHelper.invitationBuilder(institution).build());
        val URL = API_PREFIX_V1 + "/institutions/{institution}/invitations/{token}";
        mockMvc.perform(put(URL, institution.getSlug(), invitation.getToken())
                .contentType(MediaType.APPLICATION_JSON)
                .with(jwtBearerToken(member, objectMapper))
                .content(objectMapper.writeValueAsBytes(request))
        ).andExpect(status().isForbidden());
    }

    @Test
    public void resendShouldThrowBadRequestOnDifferentUrlInstitutionAndInvitationInstitution() throws Exception {
        val request = InvitationHelper.invitationRequestBuilder().build();
        val newInstitution = institutionRepository
                .save(InvitationHelper.institutionBuilder().build());
        val invitation = invitationRepository
                .save(InvitationHelper.invitationBuilder(newInstitution).build());
        val URL = API_PREFIX_V1 + "/institutions/{institution}/invitations/{token}";
        mockMvc.perform(put(URL, institution.getSlug(), invitation.getToken())
                .contentType(MediaType.APPLICATION_JSON)
                .with(jwtBearerToken(institutionAdmin, objectMapper))
                .content(objectMapper.writeValueAsBytes(request))
        ).andExpect(status().isBadRequest());
    }

    @Test
    public void shouldResend() throws Exception {
        val request = InvitationHelper.invitationRequestBuilder().build();
        val invitation = invitationRepository
                .save(InvitationHelper.invitationBuilder(institution).build());
        val URL = API_PREFIX_V1 + "/institutions/{institution}/invitations/{token}";
        mockMvc.perform(put(URL, institution.getSlug(), invitation.getToken())
                .contentType(MediaType.APPLICATION_JSON)
                .with(jwtBearerToken(institutionAdmin, objectMapper))
                .content(objectMapper.writeValueAsBytes(request))
        ).andExpect(status().isOk());

        val removedInvitation = invitationRepository
                .findByToken(invitation.getToken(), Invitation.class);
        val dbInvitation = invitationRepository
                .findByEmailAndInstitutionSlug(
                        request.getEmail(),
                        institution.getSlug(),
                        Invitation.class
                );
        assertThat(removedInvitation, isEmpty());
        assertThat(dbInvitation, isPresent());
        assertEquals(dbInvitation.get().getStatus(), InvitationStatus.WAITING);
    }

    @Test
    public void confirmShouldRequireAuthenticated() throws Exception {
        val invitation = invitationRepository.save(InvitationHelper.invitationBuilder(institution).build());
        val URL = API_PREFIX_V1 + "/invitations/{token}/confirm";
        mockMvc.perform(
                post(URL, invitation.getToken()).contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isUnauthorized());
    }

    @Test
    public void shouldConfirm() throws Exception {
        val invitation = invitationRepository.save(InvitationHelper.invitationBuilder(institution).build());
        val URL = API_PREFIX_V1 + "/invitations/{token}/confirm";
        mockMvc.perform(
                post(URL, invitation.getToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(jwtBearerToken(user, objectMapper))
        ).andExpect(status().isOk());

        val userResponse = appUserRepository.findByEmailWithRolesAndGroupsAndInstitution(user.getEmail(), AppUser.class);
        assertThat(userResponse.get().getRoles(), hasSize(1));
    }

    @Test
    public void rejectShouldHandleNFE() throws Exception {
        val invitation = InvitationHelper.invitationBuilder(institution).build();
        val URL = API_PREFIX_V1 + "/invitations/{token}/reject";
        mockMvc.perform(
                put(URL, invitation.getToken()).contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isNotFound());
    }

    @Test
    public void shouldReject() throws Exception {
        val invitation = invitationRepository.save(InvitationHelper.invitationBuilder(institution).build());
        val URL = API_PREFIX_V1 + "/invitations/{token}/reject";
        mockMvc.perform(
                put(URL, invitation.getToken()).contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk());

        val invitationAfterReject = invitationRepository.findByToken(invitation.getToken(), Invitation.class);
        assertThat(invitationAfterReject, isPresent());
        assertEquals(invitationAfterReject.get().getStatus(), InvitationStatus.REJECTED);
    }

    @Test
    public void deleteShouldThrowBadRequestOnDifferentUrlInstitutionAndInvitationInstitution() throws Exception {
        val newInstitution = institutionRepository
                .save(InvitationHelper.institutionBuilder().build());
        val invitation = invitationRepository
                .save(InvitationHelper.invitationBuilder(newInstitution).build());
        val URL = API_PREFIX_V1 + "/institutions/{institution}/invitations/{token}";
        mockMvc.perform(delete(URL, institution.getSlug(), invitation.getToken())
                .contentType(MediaType.APPLICATION_JSON)
                .with(jwtBearerToken(institutionAdmin, objectMapper))
        ).andExpect(status().isBadRequest());
    }

    @Test
    public void deleteShouldBeSecured() throws Exception {
        val invitation = invitationRepository
                .save(InvitationHelper.invitationBuilder(institution).build());
        val URL = API_PREFIX_V1 + "/institution/{institution}/invitation/{token}";
        mockMvc.perform(delete(URL, institution.getSlug(), invitation.getToken())
                .contentType(MediaType.APPLICATION_JSON)
                .with(jwtBearerToken(member, objectMapper))
        ).andExpect(status().isForbidden());
    }

    private void reset() {
        testDbHelper.clean();
    }

    private void addRoles(AppUser user, Group group, AppRole[] roles) {
        for (AppRole role: roles) {
            val roleBuild = UserRole.builder().
                    role(role)
                    .user(user)
                    .group(group)
                    .build();
            userRoleRepository.save(roleBuild);
        }
    }
}
