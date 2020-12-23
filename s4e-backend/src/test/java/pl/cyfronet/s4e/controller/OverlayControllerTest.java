/*
 * Copyright 2020 ACC Cyfronet AGH
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
import pl.cyfronet.s4e.InvitationHelper;
import pl.cyfronet.s4e.OverlayHelper;
import pl.cyfronet.s4e.TestDbHelper;
import pl.cyfronet.s4e.bean.*;
import pl.cyfronet.s4e.data.repository.*;

import java.util.List;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static pl.cyfronet.s4e.Constants.API_PREFIX_V1;
import static pl.cyfronet.s4e.TestJwtUtil.jwtBearerToken;

@BasicTest
@AutoConfigureMockMvc
public class OverlayControllerTest {
    @Autowired
    private PRGOverlayRepository prgOverlayRepository;

    @Autowired
    private WMSOverlayRepository wmsOverlayRepository;

    @Autowired
    private TestDbHelper testDbHelper;

    @Autowired
    private InstitutionRepository institutionRepository;

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private UserRoleRepository userRoleRepository;

    @Autowired
    private SldStyleRepository sldStyleRepository;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private AppUser superAdmin;
    private AppUser institutionAdmin;
    private AppUser user;
    private AppUser member;
    private Institution institution;

    private PRGOverlay prgOverlay;
    private WMSOverlay wmsGlobalOverlay;
    private WMSOverlay wmsInstitutionalOverlay;
    private WMSOverlay wmsPersonalOverlay;

    @BeforeEach
    public void beforeEach() {
        testDbHelper.clean();

        superAdmin = appUserRepository.save(InvitationHelper.userBuilder().authority("ROLE_ADMIN").build());

        institution = institutionRepository.save(InvitationHelper.institutionBuilder().build());

        institutionAdmin = appUserRepository.save(InvitationHelper.userBuilder().build());
        val institutionAdminRoles = new AppRole[]{
                AppRole.INST_ADMIN,
                AppRole.INST_MEMBER
        };
        addRoles(institutionAdmin, institution, institutionAdminRoles);

        member = appUserRepository.save(InvitationHelper.userBuilder().build());
        val memberRoles = new AppRole[]{AppRole.INST_MEMBER};
        addRoles(member, institution, memberRoles);

        user = appUserRepository.save(InvitationHelper.userBuilder().build());

        this.prepareGlobalOverlays();
        this.prepareInstitutionalOverlay();
        this.preparePersonalOverlay();
    }

    @AfterEach
    public void afterEach() {
        testDbHelper.clean();
    }

    @Test
    public void shouldGetGlobalOverlaysWhenNotLoggedIn() throws Exception {
        val URL = API_PREFIX_V1 + "/overlays";
        mockMvc.perform(get(URL)
                .contentType(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(equalTo(2))));
    }

    @Test
    public void shouldGetAllUserOverlays() throws Exception {
        val URL = API_PREFIX_V1 + "/overlays";
        mockMvc.perform(get(URL)
                .contentType(MediaType.APPLICATION_JSON)
                .with(jwtBearerToken(member, objectMapper))
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(equalTo(4))));
    }

    @Test
    public void personalOverlayCreationShouldBeSecured() throws Exception {
        val request = OverlayHelper.overlayRequestBuilder().build();
        val URL = API_PREFIX_V1 + "/overlays/personal";
        mockMvc.perform(post(URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(request))
        ).andExpect(status().isUnauthorized());
    }

    @Test
    public void shouldCreatePersonalOverlay() throws Exception {
        val request = OverlayHelper.overlayRequestBuilder().build();
        val URL = API_PREFIX_V1 + "/overlays/personal";
        mockMvc.perform(post(URL)
                .contentType(MediaType.APPLICATION_JSON)
                .with(jwtBearerToken(member, objectMapper))
                .content(objectMapper.writeValueAsBytes(request))
        ).andExpect(status().isOk());

        val personalOverlays = wmsOverlayRepository
                .findAllPersonal(member.getId(), OverlayOwner.PERSONAL);
        assertEquals(2, personalOverlays.size());
    }

    @Test
    public void globalOverlayCreationShouldBeSecured() throws Exception {
        val request = OverlayHelper.overlayRequestBuilder().build();
        val URL = API_PREFIX_V1 + "/overlays/global";
        mockMvc.perform(post(URL)
                .contentType(MediaType.APPLICATION_JSON)
                .with(jwtBearerToken(member, objectMapper))
                .content(objectMapper.writeValueAsBytes(request))
        ).andExpect(status().isForbidden());

        mockMvc.perform(post(URL)
                .contentType(MediaType.APPLICATION_JSON)
                .with(jwtBearerToken(institutionAdmin, objectMapper))
                .content(objectMapper.writeValueAsBytes(request))
        ).andExpect(status().isForbidden());
    }

    @Test
    public void shouldCreateGlobalOverlay() throws Exception {
        val request = OverlayHelper.overlayRequestBuilder().build();
        val URL = API_PREFIX_V1 + "/overlays/global";
        mockMvc.perform(post(URL)
                .contentType(MediaType.APPLICATION_JSON)
                .with(jwtBearerToken(superAdmin, objectMapper))
                .content(objectMapper.writeValueAsBytes(request))
        ).andExpect(status().isOk());

        val globalOverlays = wmsOverlayRepository
                .findAllByOwnerType(OverlayOwner.GLOBAL);
        assertEquals(3, globalOverlays.size());
    }

    @Test
    public void institutionalOverlayShouldBeSecured() throws Exception {
        val request = OverlayHelper.overlayRequestBuilder().build();
        val URL = API_PREFIX_V1 + "/institutions/{institution}/overlays";
        mockMvc.perform(post(URL, institution.getSlug())
                .contentType(MediaType.APPLICATION_JSON)
                .with(jwtBearerToken(member, objectMapper))
                .content(objectMapper.writeValueAsBytes(request))
        ).andExpect(status().isForbidden());
    }

    @Test
    public void shouldCreateInstitutionalOverlay() throws Exception {
        val request = OverlayHelper.overlayRequestBuilder().build();
        val URL = API_PREFIX_V1 + "/institutions/{institution}/overlays";
        mockMvc.perform(post(URL, institution.getSlug())
                .contentType(MediaType.APPLICATION_JSON)
                .with(jwtBearerToken(institutionAdmin, objectMapper))
                .content(objectMapper.writeValueAsBytes(request))
        ).andExpect(status().isOk());

        val institutionalOverlays = wmsOverlayRepository
                .findAllInstitutional(
                        institutionAdmin.getId(),
                        AppRole.INST_MEMBER,
                        OverlayOwner.INSTITUTIONAL
                );
        assertEquals(2, institutionalOverlays.size());
    }

    @Test
    public void deletePersonalOverlayShouldBeSecured() throws Exception {
        val URL = API_PREFIX_V1 + "/overlays/personal/{id}";
        mockMvc.perform(delete(URL, wmsPersonalOverlay.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .with(jwtBearerToken(user, objectMapper))
        ).andExpect(status().isNotFound());
    }

    @Test
    public void shouldDeletePersonalOverlay() throws Exception {
        val URL = API_PREFIX_V1 + "/overlays/personal/{id}";
        mockMvc.perform(delete(URL, wmsPersonalOverlay.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .with(jwtBearerToken(member, objectMapper))
        ).andExpect(status().isOk());

        val personalOverlays = wmsOverlayRepository
                .findAllPersonal(member.getId(), OverlayOwner.PERSONAL);
        assertEquals(0, personalOverlays.size());
    }

    @Test
    public void deleteGlobalOverlayShouldBeSecured() throws Exception {
        val URL = API_PREFIX_V1 + "/overlays/global/{id}";
        mockMvc.perform(delete(URL, wmsGlobalOverlay.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .with(jwtBearerToken(member, objectMapper))
        ).andExpect(status().isForbidden());

        mockMvc.perform(delete(URL, wmsGlobalOverlay.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .with(jwtBearerToken(institutionAdmin, objectMapper))
        ).andExpect(status().isForbidden());
    }

    @Test
    public void shouldDeleteGlobalOverlay() throws Exception {
        val URL = API_PREFIX_V1 + "/overlays/global/{id}";
        mockMvc.perform(delete(URL, wmsGlobalOverlay.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .with(jwtBearerToken(superAdmin, objectMapper))
        ).andExpect(status().isOk());

        val globalOverlays = wmsOverlayRepository
                .findAllByOwnerType(OverlayOwner.GLOBAL);
        assertEquals(1, globalOverlays.size());
    }

    @Test
    public void deleteInstitutionalOverlayShouldBeSecured() throws Exception {
        val URL = API_PREFIX_V1 + "/institutions/{institution}/overlays/{id}";
        mockMvc.perform(delete(URL, institution.getSlug(), wmsInstitutionalOverlay.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .with(jwtBearerToken(member, objectMapper))
        ).andExpect(status().isForbidden());
    }

    @Test
    public void shouldDeleteInstitutionalOverlay() throws Exception {
        val oldInstitutionalOverlaysSize = wmsOverlayRepository
                .findAllInstitutional(
                        institutionAdmin.getId(),
                        AppRole.INST_MEMBER,
                        OverlayOwner.INSTITUTIONAL
                )
                .size();
        val URL = API_PREFIX_V1 + "/institutions/{institution}/overlays/{id}";
        mockMvc.perform(delete(URL, institution.getSlug(), wmsInstitutionalOverlay.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .with(jwtBearerToken(institutionAdmin, objectMapper))
        ).andExpect(status().isOk());

        val institutionalOverlays = wmsOverlayRepository
                .findAllInstitutional(
                        institutionAdmin.getId(),
                        AppRole.INST_MEMBER,
                        OverlayOwner.INSTITUTIONAL
                );
        assertEquals(oldInstitutionalOverlaysSize - 1, institutionalOverlays.size());
    }

    @Test
    public void setOverlayAsVisibleForCertainUserShouldBeSecured() throws Exception {
        val URL = API_PREFIX_V1 + "/overlays/{id}/visible";
        mockMvc.perform(put(URL, wmsPersonalOverlay.getId())
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isUnauthorized());
    }

    @Test
    public void shouldSetOverlayAsVisibleForCertainUser() throws Exception {
        val URL = API_PREFIX_V1 + "/overlays/{id}/visible";
        mockMvc.perform(put(URL, wmsPersonalOverlay.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .with(jwtBearerToken(member, objectMapper))
        ).andExpect(status().isOk());

        val user = appUserRepository.findByEmail(member.getEmail()).get();
        val nonVisibleOverlays = (List<Long>) user.getPreferences().getNonVisibleOverlays();
        assertEquals(0, nonVisibleOverlays.size());
    }

    @Test
    public void setOverlayAsNonVisibleForCertainUserShouldBeSecured() throws Exception {
        val URL = API_PREFIX_V1 + "/overlays/{id}/visible";
        mockMvc.perform(delete(URL, wmsPersonalOverlay.getId())
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isUnauthorized());
    }

    @Test
    public void shouldSetOverlayAsNonVisibleForCertainUser() throws Exception {
        val URL = API_PREFIX_V1 + "/overlays/{id}/visible";
        mockMvc.perform(delete(URL, wmsPersonalOverlay.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .with(jwtBearerToken(member, objectMapper))
        ).andExpect(status().isOk());

        val user = appUserRepository.findByEmail(member.getEmail()).get();
        user.getPreferences();
        val nonVisibleOverlays = (List<Long>) user.getPreferences().getNonVisibleOverlays();
        assertEquals(1, nonVisibleOverlays.size());
        assertEquals(wmsPersonalOverlay.getId(), (Long) nonVisibleOverlays.get(0));
    }

    private void addRoles(AppUser user, Institution institution, AppRole[] roles) {
        for (AppRole role: roles) {
            val roleBuild = UserRole.builder().
                    role(role)
                    .user(user)
                    .institution(institution)
                    .build();
            userRoleRepository.save(roleBuild);
        }
    }

    private void prepareGlobalOverlays() {
        val prgWms = wmsOverlayRepository.save(
                OverlayHelper
                        .wmsOverlayBuilder()
                        .ownerType(OverlayOwner.GLOBAL)
                        .url("")
                        .build()
        );
        val sldStyle = sldStyleRepository.save(
                OverlayHelper
                        .sldStyleBuilder()
                        .build()
        );
        prgOverlay = prgOverlayRepository.save(
                OverlayHelper
                        .prgOverlayBuilder()
                        .wmsOverlay(prgWms)
                        .sldStyle(sldStyle)
                        .build()
        );

        wmsGlobalOverlay = wmsOverlayRepository.save(
                OverlayHelper
                        .wmsOverlayBuilder()
                        .ownerType(OverlayOwner.GLOBAL)
                        .build()
        );
    }

    private void prepareInstitutionalOverlay() {
        wmsInstitutionalOverlay = wmsOverlayRepository.save(
                OverlayHelper
                        .wmsOverlayBuilder()
                        .ownerType(OverlayOwner.INSTITUTIONAL)
                        .institution(institution)
                        .build()
        );
    }

    private void preparePersonalOverlay() {
        wmsPersonalOverlay = wmsOverlayRepository.save(
                OverlayHelper
                        .wmsOverlayBuilder()
                        .ownerType(OverlayOwner.PERSONAL)
                        .appUser(member)
                        .build()
        );
    }
}
