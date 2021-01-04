/*
 * Copyright 2021 ACC Cyfronet AGH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

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
import pl.cyfronet.s4e.InvitationTestHelper;
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
    private UserRoleRepository userRoleRepository;

    @Autowired
    private InvitationRepository invitationRepository;

    private AppUser institutionAdmin;
    private AppUser user;
    private AppUser member;
    private Institution institution;

    @BeforeEach
    public void beforeEach() {
        testDbHelper.clean();
        institution = institutionRepository.save(InvitationTestHelper.institutionBuilder().build());

        institutionAdmin = appUserRepository.save(InvitationTestHelper.userBuilder().build());
        val institutionAdminRoles = new AppRole[]{AppRole.INST_ADMIN, AppRole.INST_MEMBER};
        addRoles(institutionAdmin, institution, institutionAdminRoles);

        member = appUserRepository.save(InvitationTestHelper.userBuilder().build());
        val memberRoles = new AppRole[]{AppRole.INST_MEMBER};
        addRoles(member, institution, memberRoles);

        user = appUserRepository.save(InvitationTestHelper.userBuilder().build());
    }

    @AfterEach
    public void afterEach() {
        testDbHelper.clean();
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
                .save(InvitationTestHelper.invitationBuilder(institution).build());
        mockMvc.perform(get(URL, institution.getSlug())
                .contentType(MediaType.APPLICATION_JSON)
                .with(jwtBearerToken(institutionAdmin, objectMapper))
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(equalTo(1))));
    }

    @Test
    public void createShouldBeSecured() throws Exception {
        val request = InvitationTestHelper.invitationRequestBuilder().build();
        val URL = API_PREFIX_V1 + "/institutions/{institution}/invitations";
        mockMvc.perform(post(URL, institution.getSlug())
                .contentType(MediaType.APPLICATION_JSON)
                .with(jwtBearerToken(member, objectMapper))
                .content(objectMapper.writeValueAsBytes(request))
        ).andExpect(status().isForbidden());
    }

    @Test
    public void shouldntCreateForMember() throws Exception {
        val request = InvitationTestHelper.invitationRequestBuilder().build();
        request.setEmail(member.getEmail());
        val URL = API_PREFIX_V1 + "/institutions/{institution}/invitations";
        mockMvc.perform(post(URL, institution.getSlug())
                .contentType(MediaType.APPLICATION_JSON)
                .with(jwtBearerToken(institutionAdmin, objectMapper))
                .content(objectMapper.writeValueAsBytes(request))
        ).andExpect(status().isBadRequest());

        val dbInvitation = invitationRepository
                .findByEmailAndInstitutionSlug(
                        request.getEmail(),
                        institution.getSlug(),
                        Invitation.class
                );

        assertThat(dbInvitation, isEmpty());
    }

    @Test
    public void createShouldHandleNFE() throws Exception {
        val institution = InvitationTestHelper.institutionBuilder().build();
        val request = InvitationTestHelper.invitationRequestBuilder().build();
        val URL = API_PREFIX_V1 + "/institutions/{institution}/invitations";
        mockMvc.perform(post(URL, institution.getSlug())
                .contentType(MediaType.APPLICATION_JSON)
                .with(jwtBearerToken(institutionAdmin, objectMapper))
                .content(objectMapper.writeValueAsBytes(request))
        ).andExpect(status().isForbidden());
    }

    @Test
    public void shouldCreate() throws Exception {
        val request = InvitationTestHelper.invitationRequestBuilder().build();
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
        val invitation = invitationRepository
                .save(InvitationTestHelper.invitationBuilder(institution).build());
        val request = InvitationTestHelper.invitationResendInvitationBuilder(invitation.getEmail()).build();
        val URL = API_PREFIX_V1 + "/institutions/{institution}/invitations";
        mockMvc.perform(put(URL, institution.getSlug())
                .contentType(MediaType.APPLICATION_JSON)
                .with(jwtBearerToken(member, objectMapper))
                .content(objectMapper.writeValueAsBytes(request))
        ).andExpect(status().isForbidden());
    }

    @Test
    public void shouldResend() throws Exception {
        val invitation = invitationRepository
                .save(InvitationTestHelper.invitationBuilder(institution).build());
        val request = InvitationTestHelper.invitationResendInvitationBuilder(invitation.getEmail()).build();
        val URL = API_PREFIX_V1 + "/institutions/{institution}/invitations";
        mockMvc.perform(put(URL, institution.getSlug())
                .contentType(MediaType.APPLICATION_JSON)
                .with(jwtBearerToken(institutionAdmin, objectMapper))
                .content(objectMapper.writeValueAsBytes(request))
        ).andExpect(status().isOk());

        val removedInvitation = invitationRepository
                .findByToken(invitation.getToken(), Invitation.class);
        val dbInvitation = invitationRepository
                .findByEmailAndInstitutionSlug(
                        request.getNewEmail(),
                        institution.getSlug(),
                        Invitation.class
                );
        assertThat(removedInvitation, isEmpty());
        assertThat(dbInvitation, isPresent());
        assertEquals(dbInvitation.get().getStatus(), InvitationStatus.WAITING);
    }

    @Test
    public void confirmShouldRequireAuthenticated() throws Exception {
        val invitation = invitationRepository.save(InvitationTestHelper.invitationBuilder(institution).build());
        val URL = API_PREFIX_V1 + "/invitations/{token}/confirm";
        mockMvc.perform(
                post(URL, invitation.getToken()).contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isUnauthorized());
    }

    @Test
    public void shouldConfirm() throws Exception {
        val invitation = invitationRepository.save(InvitationTestHelper.invitationBuilder(institution).build());
        val URL = API_PREFIX_V1 + "/invitations/{token}/confirm";
        mockMvc.perform(
                post(URL, invitation.getToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(jwtBearerToken(user, objectMapper))
        ).andExpect(status().isOk());

        val userResponse = appUserRepository.findByEmailWithRolesAndInstitution(user.getEmail(), AppUser.class);
        assertThat(userResponse.get().getRoles(), hasSize(1));
    }

    @Test
    public void rejectShouldHandleNFE() throws Exception {
        val invitation = InvitationTestHelper.invitationBuilder(institution).build();
        val URL = API_PREFIX_V1 + "/invitations/{token}/reject";
        mockMvc.perform(
                put(URL, invitation.getToken()).contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isNotFound());
    }

    @Test
    public void shouldReject() throws Exception {
        val invitation = invitationRepository.save(InvitationTestHelper.invitationBuilder(institution).build());
        val URL = API_PREFIX_V1 + "/invitations/{token}/reject";
        mockMvc.perform(
                put(URL, invitation.getToken()).contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk());

        val invitationAfterReject = invitationRepository.findByToken(invitation.getToken(), Invitation.class);
        assertThat(invitationAfterReject, isPresent());
        assertEquals(invitationAfterReject.get().getStatus(), InvitationStatus.REJECTED);
    }

    @Test
    public void deleteShouldBeSecured() throws Exception {
        val invitation = invitationRepository
                .save(InvitationTestHelper.invitationBuilder(institution).build());
        val URL = API_PREFIX_V1 + "/institution/{institution}/invitation/{id}";
        mockMvc.perform(delete(URL, institution.getSlug(), invitation.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .with(jwtBearerToken(member, objectMapper))
        ).andExpect(status().isForbidden());
    }

    @Test
    public void deleteShouldThrowNotFoundExceptionOnDifferentUrlInstitutionAndInvitationInstitution() throws Exception {
        val newInstitution = institutionRepository
                .save(InvitationTestHelper.institutionBuilder().build());
        val invitation = invitationRepository
                .save(InvitationTestHelper.invitationBuilder(newInstitution).build());
        val URL = API_PREFIX_V1 + "/institutions/{institution}/invitations/{id}";
        mockMvc.perform(delete(URL, institution.getSlug(), invitation.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .with(jwtBearerToken(institutionAdmin, objectMapper))
        ).andExpect(status().isNotFound());
    }

    private void addRoles(AppUser user, Institution institution, AppRole[] roles) {
        for (AppRole role : roles) {
            val roleBuild = UserRole.builder().
                    role(role)
                    .user(user)
                    .institution(institution)
                    .build();
            userRoleRepository.save(roleBuild);
        }
    }
}
