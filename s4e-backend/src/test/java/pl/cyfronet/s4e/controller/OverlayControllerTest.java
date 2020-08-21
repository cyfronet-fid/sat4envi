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
    private GroupRepository groupRepository;

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

        superAdmin = appUserRepository.save(InvitationHelper.userBuilder().admin(true).build());

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
                .findAllByOwnerTypeAndAppUserId(OverlayOwner.PERSONAL, member.getId(), WMSOverlay.class);
        assertEquals(personalOverlays.size(), 2);
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

        val globalOverlays = wmsOverlayRepository.findAllByOwnerType(OverlayOwner.GLOBAL, WMSOverlay.class);
        assertEquals(globalOverlays.size(), 3);
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
                .findAllByOwnerTypeAndInstitutionId(OverlayOwner.INSTITUTIONAL, institution.getId(), WMSOverlay.class);
        assertEquals(institutionalOverlays.size(), 2);
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
                .findAllByOwnerTypeAndAppUserId(OverlayOwner.PERSONAL, member.getId(), WMSOverlay.class);
        assertEquals(personalOverlays.size(), 0);
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
                .findAllByOwnerType(OverlayOwner.GLOBAL, WMSOverlay.class);
        assertEquals(globalOverlays.size(), 1);
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
        val URL = API_PREFIX_V1 + "/institutions/{institution}/overlays/{id}";
        mockMvc.perform(delete(URL, institution.getSlug(), wmsInstitutionalOverlay.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .with(jwtBearerToken(institutionAdmin, objectMapper))
        ).andExpect(status().isOk());

        val institutionalOverlays = wmsOverlayRepository
                .findAllByOwnerTypeAndInstitutionId(OverlayOwner.INSTITUTIONAL, institution.getId(), WMSOverlay.class);
        assertEquals(institutionalOverlays.size(), 0);
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
        assertEquals(nonVisibleOverlays.size(), 0);
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
        assertEquals(nonVisibleOverlays.size(), 1);
        assertEquals((Long) nonVisibleOverlays.get(0), wmsPersonalOverlay.getId());
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