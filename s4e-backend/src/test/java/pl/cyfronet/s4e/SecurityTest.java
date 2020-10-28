package pl.cyfronet.s4e;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import pl.cyfronet.s4e.bean.AppRole;
import pl.cyfronet.s4e.bean.AppUser;
import pl.cyfronet.s4e.controller.request.DeleteUserRoleRequest;
import pl.cyfronet.s4e.data.repository.AppUserRepository;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static pl.cyfronet.s4e.Constants.API_PREFIX_V1;
import static pl.cyfronet.s4e.TestJwtUtil.jwtBearerToken;

@BasicTest
@Slf4j
@AutoConfigureMockMvc
public class SecurityTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private AppUserRepository appUserRepository;
    @Autowired
    private TestDbHelper testDbHelper;

    private AppUser securityAppUser;

    @BeforeEach
    public void beforeEach() {
        reset();

        securityAppUser = appUserRepository.save(AppUser.builder()
                .email("get@profile.com")
                .name("Get")
                .surname("Profile")
                .password("{noop}password")
                .enabled(true)
                .build());
    }

    @AfterEach
    public void afterEach() {
        reset();
    }

    private void reset() {
        testDbHelper.clean();
    }

    // 401
    @Test
    public void shouldReturn401ForUserWithoutAuthentication() throws Exception {
        mockMvc.perform(get(API_PREFIX_V1 + "/users/me"))
                .andExpect(status().isUnauthorized());
    }

    // 401
    @Test
    public void shouldReturn401ForUserWithBadAuthentication() throws Exception {
        AppUser appUser = AppUser.builder()
                .email("badCredentials@profile.com")
                .name("Get")
                .surname("Profile")
                .password("{noop}badPassword")
                .enabled(true)
                .build();

        mockMvc.perform(get(API_PREFIX_V1 + "/users/me")
                .with(jwtBearerToken(appUser, objectMapper)))
                .andExpect(status().isUnauthorized());
    }

    // 403
    @Test
    public void shouldReturn403ForAuthenticatedWithoutAuthorization() throws Exception {
        mockMvc.perform(post(API_PREFIX_V1 + "/user-role")
                .contentType(MediaType.APPLICATION_JSON)
                .with(jwtBearerToken(securityAppUser, objectMapper)))
                .andExpect(status().isForbidden());
    }

    // 404
    @Test
    public void shouldReturn404ForAuthenticatedAndAuthorizedUserNoResource() throws Exception {
        securityAppUser.setAdmin(true);
        appUserRepository.save(securityAppUser);
        DeleteUserRoleRequest userRoleRequest = DeleteUserRoleRequest.builder()
                .email("profile@email")
                .institutionSlug("slugInstitution")
                .role(AppRole.INST_ADMIN)
                .build();

        mockMvc.perform(delete(API_PREFIX_V1 + "/user-role")
                .contentType(MediaType.APPLICATION_JSON)
                .with(jwtBearerToken(securityAppUser, objectMapper))
                .content(objectMapper.writeValueAsBytes(userRoleRequest)))
                .andExpect(status().isNotFound());
    }
}
