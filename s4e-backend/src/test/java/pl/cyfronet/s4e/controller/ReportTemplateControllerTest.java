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
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import pl.cyfronet.s4e.BasicTest;
import pl.cyfronet.s4e.TestDbHelper;
import pl.cyfronet.s4e.bean.AppUser;
import pl.cyfronet.s4e.bean.ReportTemplate;
import pl.cyfronet.s4e.controller.request.CreateReportTemplateRequest;
import pl.cyfronet.s4e.data.repository.AppUserRepository;
import pl.cyfronet.s4e.data.repository.ReportTemplateRepository;

import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static pl.cyfronet.s4e.Constants.API_PREFIX_V1;
import static pl.cyfronet.s4e.TestJwtUtil.jwtBearerToken;

@AutoConfigureMockMvc
@BasicTest
@Slf4j
public class ReportTemplateControllerTest {
    private static final Pattern UUID_PATTERN = Pattern.compile("[0-9A-Za-z]{8}-[0-9A-Za-z]{4}-[0-9A-Za-z]{4}-[0-9A-Za-z]{4}-[0-9A-Za-z]{12}");
    private static final Pattern DATETIME_PATTERN = Pattern.compile("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.\\d+Z");

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private ReportTemplateRepository reportTemplateRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TestDbHelper testDbHelper;

    private AppUser appUser;

    @BeforeEach
    public void beforeEach() {
        testDbHelper.clean();

        appUser = appUserRepository.save(AppUser.builder()
                .email("get@profile.com")
                .name("Get")
                .surname("Profile")
                .password("{noop}password")
                .enabled(true)
                .build());
    }

    public interface ReportTemplateProjection {
        UUID getId();
        OwnerProjection getOwner();
        String getCaption();
        String getNotes();
        List<Long> getOverlayIds();
        Long getProductId();
    }

    public interface OwnerProjection {
        String getEmail();
    }

    private static final String REPORT_TEMPLATES_URL_PREFIX = API_PREFIX_V1 + "/report-templates";

    @Nested
    class CreateEndpoint {
        @Test
        public void shouldWork() throws Exception {
            CreateReportTemplateRequest request = CreateReportTemplateRequest.builder()
                    .caption("some caption")
                    .notes("yet some notes")
                    .overlayIds(List.of(42L, 137L))
                    .productId(43L)
                    .build();

            assertThat(reportTemplateRepository.count(), is(equalTo(0L)));

            mockMvc.perform(post(REPORT_TEMPLATES_URL_PREFIX)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsBytes(request))
                    .with(jwtBearerToken(appUser, objectMapper)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.uuid", matchesPattern(UUID_PATTERN)))
                    .andExpect(jsonPath("$.caption", is(equalTo("some caption"))))
                    .andExpect(jsonPath("$.notes", is(equalTo("yet some notes"))))
                    .andExpect(jsonPath("$.overlayIds", contains(42, 137)))
                    .andExpect(jsonPath("$.productId", is(equalTo(43))))
                    .andExpect(jsonPath("$.createdAt", matchesPattern(DATETIME_PATTERN)));

            val allTemplates = reportTemplateRepository.findAllFetchOwnerBy(ReportTemplateProjection.class);

            assertThat(allTemplates, hasSize(1));
            val template = allTemplates.get(0);
            assertThat(template.getOwner().getEmail(), is(equalTo("get@profile.com")));
            assertThat(template, allOf(
                    hasProperty("caption", equalTo("some caption")),
                    hasProperty("notes", equalTo("yet some notes")),
                    hasProperty("overlayIds", equalTo(List.of(42L, 137L))),
                    hasProperty("productId", equalTo(43L))
            ));
        }

        @Test
        public void shouldWorkForEmptyPayload() throws Exception {
            CreateReportTemplateRequest request = CreateReportTemplateRequest.builder().build();

            assertThat(reportTemplateRepository.count(), is(equalTo(0L)));

            mockMvc.perform(post(REPORT_TEMPLATES_URL_PREFIX)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsBytes(request))
                    .with(jwtBearerToken(appUser, objectMapper)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.uuid", matchesPattern(UUID_PATTERN)))
                    .andExpect(jsonPath("$.caption", is(nullValue())))
                    .andExpect(jsonPath("$.notes", is(nullValue())))
                    .andExpect(jsonPath("$.overlayIds", hasSize(0)))
                    .andExpect(jsonPath("$.productId", is(nullValue())))
                    .andExpect(jsonPath("$.createdAt", matchesPattern(DATETIME_PATTERN)));

            val allTemplates = reportTemplateRepository.findAllFetchOwnerBy(ReportTemplateProjection.class);

            assertThat(allTemplates, hasSize(1));
            val template = allTemplates.get(0);
            assertThat(template.getOwner().getEmail(), is(equalTo("get@profile.com")));
            assertThat(template, allOf(
                    hasProperty("caption", equalTo(null)),
                    hasProperty("notes", equalTo(null)),
                    hasProperty("overlayIds", equalTo(List.of())),
                    hasProperty("productId", equalTo(null))
            ));
        }

        @Test
        public void shouldReturn401IfUserNotLoggedIn() throws Exception {
            CreateReportTemplateRequest request = CreateReportTemplateRequest.builder().build();

            assertThat(reportTemplateRepository.count(), is(equalTo(0L)));

            mockMvc.perform(post(REPORT_TEMPLATES_URL_PREFIX)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsBytes(request)))
                    .andExpect(status().isUnauthorized());

            assertThat(reportTemplateRepository.count(), is(equalTo(0L)));
        }
    }

    @Nested
    class ListEndpoint {
        private AppUser otherAppUser;

        @BeforeEach
        public void beforeEach() {
            otherAppUser = appUserRepository.save(AppUser.builder()
                    .email("other@user.com")
                    .name("Get")
                    .surname("Profile")
                    .password("{noop}password")
                    .enabled(true)
                    .build());

            Stream.of(
                    ReportTemplate.builder()
                            .owner(appUser)
                            .caption("caption-1")
                            .build(),
                    ReportTemplate.builder()
                            .owner(appUser)
                            .caption("caption-2")
                            .build(),
                    ReportTemplate.builder()
                            .owner(otherAppUser)
                            .caption("caption-3")
                            .build()
            ).forEach(reportTemplateRepository::save);
        }

        @Test
        public void shouldWork() throws Exception {
            assertThat(reportTemplateRepository.count(), is(equalTo(3L)));

            mockMvc.perform(get(REPORT_TEMPLATES_URL_PREFIX)
                    .with(jwtBearerToken(appUser, objectMapper)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()", is(equalTo(2))))
                    .andExpect(jsonPath("$..caption", contains("caption-1", "caption-2")));

            mockMvc.perform(get(REPORT_TEMPLATES_URL_PREFIX)
                    .with(jwtBearerToken(otherAppUser, objectMapper)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()", is(equalTo(1))))
                    .andExpect(jsonPath("$..caption", contains("caption-3")));

            assertThat(reportTemplateRepository.count(), is(equalTo(3L)));
        }

        @Test
        public void shouldReturn401IfUserNotLoggedIn() throws Exception {
            mockMvc.perform(get(REPORT_TEMPLATES_URL_PREFIX))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    class DeleteEndpoint {
        private ReportTemplate reportTemplate;

        @BeforeEach
        public void beforeEach() {
            reportTemplate = reportTemplateRepository.save(ReportTemplate.builder()
                    .owner(appUser)
                    .build());
        }

        @Test
        public void shouldWork() throws Exception {
            assertThat(reportTemplateRepository.count(), is(equalTo(1L)));

            mockMvc.perform(delete(REPORT_TEMPLATES_URL_PREFIX + "/{uuid}", reportTemplate.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .with(jwtBearerToken(appUser, objectMapper)))
                    .andExpect(status().isOk());

            assertThat(reportTemplateRepository.count(), is(equalTo(0L)));
        }

        @Test
        public void shouldReturn403ForNonExistentReportTemplate() throws Exception {
            assertThat(reportTemplateRepository.count(), is(equalTo(1L)));

            mockMvc.perform(delete(REPORT_TEMPLATES_URL_PREFIX + "/{uuid}", "6dea77af-1de9-4559-be06-0d50b7db1f35")
                    .contentType(MediaType.APPLICATION_JSON)
                    .with(jwtBearerToken(appUser, objectMapper)))
                    .andExpect(status().isForbidden());

            assertThat(reportTemplateRepository.count(), is(equalTo(1L)));
        }

        @Nested
        class WithOtherUser {
            private AppUser otherAppUser;

            @BeforeEach
            public void beforeEach() {
                otherAppUser = appUserRepository.save(AppUser.builder()
                        .email("other@user.com")
                        .name("Get")
                        .surname("Profile")
                        .password("{noop}password")
                        .enabled(true)
                        .build());
            }

            @Test
            public void shouldReturn403() throws Exception {
                assertThat(reportTemplateRepository.count(), is(equalTo(1L)));

                mockMvc.perform(delete(REPORT_TEMPLATES_URL_PREFIX + "/{uuid}", reportTemplate.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(jwtBearerToken(otherAppUser, objectMapper)))
                        .andExpect(status().isForbidden());

                assertThat(reportTemplateRepository.count(), is(equalTo(1L)));
            }
        }
    }
}
