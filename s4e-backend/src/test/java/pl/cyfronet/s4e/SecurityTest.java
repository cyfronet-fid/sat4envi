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

package pl.cyfronet.s4e;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import pl.cyfronet.s4e.bean.AppRole;
import pl.cyfronet.s4e.bean.AppUser;
import pl.cyfronet.s4e.controller.request.DeleteUserRoleRequest;
import pl.cyfronet.s4e.data.repository.AppUserRepository;
import pl.cyfronet.s4e.security.SecurityConstants;

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
    @ParameterizedTest
    @ValueSource(strings = { "", " " })
    public void shouldReturn401ForUserWithBlankAuthentication(String blankToken) throws Exception {
        mockMvc.perform(get(API_PREFIX_V1 + "/users/me")
                .with(mockRequest -> {
                    mockRequest.addHeader(SecurityConstants.HEADER_NAME, "Bearer " + blankToken);
                    return mockRequest;
                }))
                .andExpect(status().isUnauthorized());
    }

    // 401
    @Test
    public void shouldReturn401ForUserWithBadAuthentication() throws Exception {
        AppUser appUser = AppUser.builder()
                .id(Long.MAX_VALUE)
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
        securityAppUser.addAuthority("ROLE_ADMIN");
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
