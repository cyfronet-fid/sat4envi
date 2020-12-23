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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import pl.cyfronet.s4e.BasicTest;
import pl.cyfronet.s4e.TestDbHelper;
import pl.cyfronet.s4e.bean.AppRole;
import pl.cyfronet.s4e.bean.AppUser;
import pl.cyfronet.s4e.bean.Institution;
import pl.cyfronet.s4e.bean.UserRole;
import pl.cyfronet.s4e.controller.request.AddMemberRequest;
import pl.cyfronet.s4e.data.repository.AppUserRepository;
import pl.cyfronet.s4e.data.repository.InstitutionRepository;
import pl.cyfronet.s4e.data.repository.UserRoleRepository;
import pl.cyfronet.s4e.service.SlugService;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static pl.cyfronet.s4e.Constants.API_PREFIX_V1;
import static pl.cyfronet.s4e.TestJwtUtil.jwtBearerToken;

@BasicTest
@AutoConfigureMockMvc
public class InstitutionControllerTest {
    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private InstitutionRepository institutionRepository;

    @Autowired
    private UserRoleRepository userRoleRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TestDbHelper testDbHelper;

    @Autowired
    private SlugService slugService;

    @Autowired
    private MockMvc mockMvc;

    private AppUser appUser;
    private AppUser instAdmin;
    private AppUser admin;

    private final String testInstitution = "Test Institution";
    private String slugInstitution;
    private Institution institution;

    @BeforeEach
    public void beforeEach() {
        testDbHelper.clean();
        appUser = appUserRepository.save(AppUser.builder()
                .email("user@mail.pl")
                .name("Get")
                .surname("Profile")
                .password("{noop}password")
                .enabled(true)
                .build());

        instAdmin = appUserRepository.save(AppUser.builder()
                .email("instAdmin@mail.pl")
                .name("Get")
                .surname("Profile")
                .password("{noop}password")
                .enabled(true)
                .build());

        admin = appUserRepository.save(AppUser.builder()
                .email("admin@mail.pl")
                .name("Get")
                .surname("Profile")
                .password("{noop}password")
                .enabled(true)
                .authority("ROLE_ADMIN")
                .build());

        slugInstitution = slugService.slugify(testInstitution);
        institution = institutionRepository.save(Institution.builder()
                .name(testInstitution)
                .slug(slugInstitution)
                .build());

        userRoleRepository.save(UserRole.builder()
                .role(AppRole.INST_MEMBER)
                .user(instAdmin)
                .institution(institution)
                .build());
        userRoleRepository.save(UserRole.builder()
                .role(AppRole.INST_ADMIN)
                .user(instAdmin)
                .institution(institution)
                .build());
    }

    @AfterEach
    public void afterEach() {
        testDbHelper.clean();
    }

    @Test
    public void shouldGetUserInstitution() throws Exception {
        mockMvc.perform(get(API_PREFIX_V1 + "/institutions")
                .contentType(MediaType.APPLICATION_JSON)
                .with(jwtBearerToken(instAdmin, objectMapper)))
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
                .password("{noop}password")
                .enabled(true)
                .build());

        mockMvc.perform(get(API_PREFIX_V1 + "/institutions")
                .contentType(MediaType.APPLICATION_JSON)
                .with(jwtBearerToken(appUser, objectMapper)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(equalTo(0))));
    }

    @Test
    public void shouldGetAllInstitutionsForUser() throws Exception {
        String testInstitutionZK = "Test Institution ZK";
        String slugInstitutionZK = slugService.slugify(testInstitutionZK);
        Institution institution = institutionRepository.save(Institution.builder()
                .name(testInstitutionZK)
                .slug(slugInstitutionZK)
                .build());

        UserRole userRole = UserRole.builder().role(AppRole.INST_MEMBER).user(appUser).institution(institution).build();
        userRoleRepository.save(userRole);

        String testInstitutionZK_MAZ = "Test Institution ZK - Mazowieckie";
        String slugInstitutionZK_MAZ = slugService.slugify(testInstitutionZK_MAZ);
        Institution institution2 = institutionRepository.save(Institution.builder()
                .name(testInstitutionZK_MAZ)
                .slug(slugInstitutionZK_MAZ)
                .parent(institution)
                .build());

        userRole = UserRole.builder().role(AppRole.INST_MEMBER).user(appUser).institution(institution2).build();
        userRoleRepository.save(userRole);

        String testInstitutionPAK = "Test Institution PAK";
        String slugInstitutionPAK = slugService.slugify(testInstitutionPAK);
        institutionRepository.save(Institution.builder()
                .name(testInstitutionPAK)
                .slug(slugInstitutionPAK)
                .build());

        mockMvc.perform(get(API_PREFIX_V1 + "/institutions")
                .contentType(MediaType.APPLICATION_JSON)
                .with(jwtBearerToken(appUser, objectMapper)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(equalTo(2))));
    }

    @Test
    public void shouldGetAllInstitutionsForAdmin() throws Exception {
        String testInstitutionZK = "Test Institution ZK";
        String slugInstitutionZK = slugService.slugify(testInstitutionZK);
        Institution institution = institutionRepository.save(Institution.builder()
                .name(testInstitutionZK)
                .slug(slugInstitutionZK)
                .build());

        String testInstitutionZK_MAZ = "Test Institution ZK - Mazowieckie";
        String slugInstitutionZK_MAZ = slugService.slugify(testInstitutionZK_MAZ);
        institutionRepository.save(Institution.builder()
                .name(testInstitutionZK_MAZ)
                .slug(slugInstitutionZK_MAZ)
                .parent(institution)
                .build());

        String testInstitutionPAK = "Test Institution PAK";
        String slugInstitutionPAK = slugService.slugify(testInstitutionPAK);
        institutionRepository.save(Institution.builder()
                .name(testInstitutionPAK)
                .slug(slugInstitutionPAK)
                .build());

        mockMvc.perform(get(API_PREFIX_V1 + "/institutions")
                .contentType(MediaType.APPLICATION_JSON)
                .with(jwtBearerToken(admin, objectMapper)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(equalTo(4))));
    }

    @Test
    public void shouldGetInstitutionForAdmin() throws Exception {
        String testInstitutionZK = "Test Institution ZK";
        String slugInstitutionZK = slugService.slugify(testInstitutionZK);
        institutionRepository.save(Institution.builder()
                .name(testInstitutionZK)
                .slug(slugInstitutionZK)
                .build());

        mockMvc.perform(get(API_PREFIX_V1 + "/institutions/{institution}", slugInstitutionZK)
                .contentType(MediaType.APPLICATION_JSON)
                .with(jwtBearerToken(admin, objectMapper)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(equalTo(10))))
                .andExpect(jsonPath("$.slug", is(equalTo(slugInstitutionZK))));
    }

    @Test
    public void shouldGetInstitutionForMember() throws Exception {
        String testInstitutionZK = "Test Institution ZK";
        String slugInstitutionZK = slugService.slugify(testInstitutionZK);
        institutionRepository.save(Institution.builder()
                .name(testInstitutionZK)
                .slug(slugInstitutionZK)
                .build());

        mockMvc.perform(get(API_PREFIX_V1 + "/institutions/{institution}", slugInstitutionZK)
                .contentType(MediaType.APPLICATION_JSON)
                .with(jwtBearerToken(admin, objectMapper)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(equalTo(10))))
                .andExpect(jsonPath("$.slug", is(equalTo(slugInstitutionZK))));
    }

    @Test
    public void shouldReturnUnauthorizedForInstitution() throws Exception {
        String testInstitutionZK = "Test Institution ZK";
        String slugInstitutionZK = slugService.slugify(testInstitutionZK);
        institutionRepository.save(Institution.builder()
                .name(testInstitutionZK)
                .slug(slugInstitutionZK)
                .build());

        mockMvc.perform(get(API_PREFIX_V1 + "/institutions/{institution}", slugInstitutionZK)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void shouldReturnUnauthorizedForInstitutions() throws Exception {
        mockMvc.perform(get(API_PREFIX_V1 + "/institutions")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void shouldReturnForbiddenForInstitution() throws Exception {
        String testInstitutionZK = "Test Institution ZK";
        String slugInstitutionZK = slugService.slugify(testInstitutionZK);
        institutionRepository.save(Institution.builder()
                .name(testInstitutionZK)
                .slug(slugInstitutionZK)
                .build());

        mockMvc.perform(get(API_PREFIX_V1 + "/institutions/{institution}", slugInstitutionZK)
                .with(jwtBearerToken(appUser, objectMapper)))
                .andExpect(status().isForbidden());
    }

    @Nested
    class GetMembers {
        @Test
        public void shouldWork() throws Exception {
            mockMvc.perform(get(API_PREFIX_V1 + "/institutions/{institution}/members", slugInstitution)
                    .contentType(MediaType.APPLICATION_JSON)
                    .with(jwtBearerToken(instAdmin, objectMapper)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()", is(equalTo(1))));
        }

        @Test
        public void shouldReturnEmptyIfNoMembers() throws Exception {
            String testInstitutionZK = "Test Institution ZK";
            String slugInstitutionZK = slugService.slugify(testInstitutionZK);
            institutionRepository.save(Institution.builder()
                    .name(testInstitutionZK)
                    .slug(slugInstitutionZK)
                    .build());

            mockMvc.perform(get(API_PREFIX_V1 + "/institutions/{institution}/members", slugInstitutionZK)
                    .contentType(MediaType.APPLICATION_JSON)
                    .with(jwtBearerToken(admin, objectMapper)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()", is(equalTo(0))));
        }

        @Test
        public void shouldBeSecured() throws Exception {
            mockMvc.perform(get(API_PREFIX_V1 + "/institutions/{institution}/members", slugInstitution)
                    .contentType(MediaType.APPLICATION_JSON)
                    .with(jwtBearerToken(appUser, objectMapper)))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    class AddMembers {
        @Test
        public void shouldWork() throws Exception {
            AddMemberRequest request = AddMemberRequest.builder().email(appUser.getEmail()).build();
            mockMvc.perform(post(API_PREFIX_V1 + "/institutions/{institution}/members", slugInstitution)
                    .contentType(MediaType.APPLICATION_JSON)
                    .with(jwtBearerToken(instAdmin, objectMapper))
                    .content(objectMapper.writeValueAsBytes(request)))
                    .andExpect(status().isOk());

            mockMvc.perform(get(API_PREFIX_V1 + "/institutions/{institution}/members", slugInstitution)
                    .contentType(MediaType.APPLICATION_JSON)
                    .with(jwtBearerToken(instAdmin, objectMapper)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()", is(equalTo(2))));
        }

        @Test
        public void shouldBeSecured() throws Exception {
            AddMemberRequest request = AddMemberRequest.builder().build();
            mockMvc.perform(post(API_PREFIX_V1 + "/institutions/{institution}/members", slugInstitution)
                    .contentType(MediaType.APPLICATION_JSON)
                    .with(jwtBearerToken(appUser, objectMapper))
                    .content(objectMapper.writeValueAsBytes(request)))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    class RemoveMembers {
        @Test
        public void shouldWork() throws Exception {
            AddMemberRequest request = AddMemberRequest.builder().email(appUser.getEmail()).build();
            mockMvc.perform(post(API_PREFIX_V1 + "/institutions/{institution}/members", slugInstitution)
                    .contentType(MediaType.APPLICATION_JSON)
                    .with(jwtBearerToken(instAdmin, objectMapper))
                    .content(objectMapper.writeValueAsBytes(request)))
                    .andExpect(status().isOk());

            mockMvc.perform(get(API_PREFIX_V1 + "/institutions/{institution}/members", slugInstitution)
                    .contentType(MediaType.APPLICATION_JSON)
                    .with(jwtBearerToken(instAdmin, objectMapper)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()", is(equalTo(2))));

            mockMvc.perform(delete(API_PREFIX_V1 + "/institutions/{institution}/members/{id}", slugInstitution, appUser.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .with(jwtBearerToken(instAdmin, objectMapper)))
                    .andExpect(status().isOk());

            mockMvc.perform(get(API_PREFIX_V1 + "/institutions/{institution}/members", slugInstitution)
                    .contentType(MediaType.APPLICATION_JSON)
                    .with(jwtBearerToken(instAdmin, objectMapper)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()", is(equalTo(1))));
        }

        @Test
        public void shouldBeSecured() throws Exception {
            mockMvc.perform(delete(API_PREFIX_V1 + "/institutions/{institution}/members/{id}", slugInstitution, appUser.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .with(jwtBearerToken(appUser, objectMapper)))
                    .andExpect(status().isForbidden());

            AddMemberRequest request = AddMemberRequest.builder().email(appUser.getEmail()).build();
            mockMvc.perform(post(API_PREFIX_V1 + "/institutions/{institution}/members", slugInstitution)
                    .contentType(MediaType.APPLICATION_JSON)
                    .with(jwtBearerToken(instAdmin, objectMapper))
                    .content(objectMapper.writeValueAsBytes(request)))
                    .andExpect(status().isOk());

            mockMvc.perform(delete(API_PREFIX_V1 + "/institutions/{institution}/members/{id}", slugInstitution, appUser.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .with(jwtBearerToken(appUser, objectMapper)))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    class DeleteInstitution {
        @BeforeEach
        public void beforeEach() {
            AppUser instAdminDeleter = appUserRepository.save(AppUser.builder()
                    .email("instAdminDeleter@mail.pl")
                    .name("Get")
                    .surname("Profile")
                    .password("{noop}password")
                    .enabled(true)
                    .authority("OP_INSTITUTION_DELETE")
                    .build());

            userRoleRepository.save(UserRole.builder()
                    .role(AppRole.INST_MEMBER)
                    .user(instAdminDeleter)
                    .institution(institution)
                    .build());
            userRoleRepository.save(UserRole.builder()
                    .role(AppRole.INST_ADMIN)
                    .user(instAdminDeleter)
                    .institution(institution)
                    .build());
        }

        private AppUser user(String name) {
            return appUserRepository.findByEmail(name + "@mail.pl").get();
        }

        @ParameterizedTest
        @ValueSource(strings = { "admin" })
        public void shouldAllowChild(String user) throws Exception {
            String childSlugInstitution = slugService.slugify(testInstitution+" child");
            institutionRepository.save(Institution.builder()
                    .name(testInstitution+" child")
                    .slug(childSlugInstitution)
                    .parent(institutionRepository.findBySlug(slugInstitution, Institution.class).get())
                    .build());

            mockMvc.perform(delete(API_PREFIX_V1 + "/institutions/{institution}", childSlugInstitution)
                    .contentType(MediaType.APPLICATION_JSON)
                    .with(jwtBearerToken(user(user), objectMapper)))
                    .andExpect(status().isOk());

            mockMvc.perform(get(API_PREFIX_V1 + "/institutions/{institution}", slugInstitution)
                    .contentType(MediaType.APPLICATION_JSON)
                    .with(jwtBearerToken(admin, objectMapper)))
                    .andExpect(status().isOk());

            mockMvc.perform(get(API_PREFIX_V1 + "/institutions/{institution}", childSlugInstitution)
                    .contentType(MediaType.APPLICATION_JSON)
                    .with(jwtBearerToken(admin, objectMapper)))
                    .andExpect(status().isNotFound());
        }

        @ParameterizedTest
        @ValueSource(strings = { "instAdminDeleter" })
        public void shouldForbidChild(String user) throws Exception {
            String childSlugInstitution = slugService.slugify(testInstitution+" child");
            institutionRepository.save(Institution.builder()
                    .name(testInstitution+" child")
                    .slug(childSlugInstitution)
                    .parent(institutionRepository.findBySlug(slugInstitution, Institution.class).get())
                    .build());

            mockMvc.perform(delete(API_PREFIX_V1 + "/institutions/{institution}", childSlugInstitution)
                    .contentType(MediaType.APPLICATION_JSON)
                    .with(jwtBearerToken(user(user), objectMapper)))
                    .andExpect(status().isForbidden());

            mockMvc.perform(get(API_PREFIX_V1 + "/institutions/{institution}", slugInstitution)
                    .contentType(MediaType.APPLICATION_JSON)
                    .with(jwtBearerToken(admin, objectMapper)))
                    .andExpect(status().isOk());

            mockMvc.perform(get(API_PREFIX_V1 + "/institutions/{institution}", childSlugInstitution)
                    .contentType(MediaType.APPLICATION_JSON)
                    .with(jwtBearerToken(admin, objectMapper)))
                    .andExpect(status().isOk());
        }

        @ParameterizedTest
        @ValueSource(strings = { "admin", "instAdminDeleter" })
        public void shouldAllowParent(String user) throws Exception {
            String childSlugInstitution = slugService.slugify(testInstitution+" child");
            institutionRepository.save(Institution.builder()
                    .name(testInstitution+" child")
                    .slug(childSlugInstitution)
                    .parent(institutionRepository.findBySlug(slugInstitution, Institution.class).get())
                    .build());

            mockMvc.perform(delete(API_PREFIX_V1 + "/institutions/{institution}", slugInstitution)
                    .contentType(MediaType.APPLICATION_JSON)
                    .with(jwtBearerToken(user(user), objectMapper)))
                    .andExpect(status().isOk());

            mockMvc.perform(get(API_PREFIX_V1 + "/institutions/{institution}", slugInstitution)
                    .contentType(MediaType.APPLICATION_JSON)
                    .with(jwtBearerToken(admin, objectMapper)))
                    .andExpect(status().isNotFound());

            mockMvc.perform(get(API_PREFIX_V1 + "/institutions/{institution}", childSlugInstitution)
                    .contentType(MediaType.APPLICATION_JSON)
                    .with(jwtBearerToken(admin, objectMapper)))
                    .andExpect(status().isNotFound());
        }

        @Test
        public void shouldBeSecured() throws Exception {
            mockMvc.perform(delete(API_PREFIX_V1 + "/institutions/{institution}", slugInstitution)
                    .contentType(MediaType.APPLICATION_JSON)
                    .with(jwtBearerToken(instAdmin, objectMapper)))
                    .andExpect(status().isForbidden());

            mockMvc.perform(delete(API_PREFIX_V1 + "/institutions/{institution}", slugInstitution)
                    .contentType(MediaType.APPLICATION_JSON)
                    .with(jwtBearerToken(admin, objectMapper)))
                    .andExpect(status().isOk());
        }
    }
}
