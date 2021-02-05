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

package pl.cyfronet.s4e.sync.log;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.HttpMethod;
import org.springframework.test.web.servlet.MockMvc;
import pl.cyfronet.s4e.BasicTest;
import pl.cyfronet.s4e.TestDbHelper;
import pl.cyfronet.s4e.bean.AppUser;
import pl.cyfronet.s4e.bean.SyncRecord;
import pl.cyfronet.s4e.data.repository.AppUserRepository;
import pl.cyfronet.s4e.data.repository.SyncRecordRepository;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static pl.cyfronet.s4e.Constants.API_PREFIX_V1;
import static pl.cyfronet.s4e.TestJwtUtil.jwtBearerToken;


@BasicTest
@AutoConfigureMockMvc
class SyncRecordControllerTest {
    private static final String URL = API_PREFIX_V1 + "/sync-records";

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private SyncRecordRepository syncRecordRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TestDbHelper testDbHelper;

    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    public void beforeEach() {
        testDbHelper.clean();
    }

    private AppUser persistUser(String... authorities) {
        return appUserRepository.save(AppUser.builder()
                .email("user@mail.pl")
                .name("John")
                .surname("Smith")
                .password("{noop}password")
                .enabled(true)
                .authorities(Arrays.asList(authorities))
                .build());
    }

    @ParameterizedTest
    @CsvSource({
            "200,GET,ROLE_ADMIN",
            "200,GET,OP_SYNC_RECORD_READ",
            "401,GET,",
            "403,GET,SOME_OTHER_AUTHORITY",
            "204,DELETE,ROLE_ADMIN",
            "401,DELETE,",
            "403,DELETE,OP_SYNC_RECORD_READ",
            "403,DELETE,SOME_OTHER_AUTHORITY",
    })
    public void shouldBeSecured(int status, HttpMethod method, String authority) throws Exception {
        val user = authority != null ? persistUser(authority) : null;

        mockMvc.perform(request(method, URL)
                .with(jwtBearerToken(user, objectMapper)))
                .andExpect(status().is(status));
    }

    @Nested
    class WithSyncRecords {
        private AppUser user;

        @BeforeEach
        public void beforeEach() {
            Stream.of(
                    SyncRecord.builder()
                            .initiatedByMethod("foo")
                            .sceneKey("foo")
                            .eventName("foo")
                            .resultCode("foo")
                            .productName("foo")
                            .build(),
                    SyncRecord.builder()
                            .sceneKey("id:1")
                            .receivedAt(LocalDateTime.of(2020, 1, 1, 0, 0))
                            .sensingTime(LocalDateTime.of(2020, 1, 1, 0, 0))
                            .build(),
                    SyncRecord.builder()
                            .sceneKey("id:2")
                            .receivedAt(LocalDateTime.of(2021, 1, 1, 0, 0))
                            .sensingTime(LocalDateTime.of(2021, 1, 1, 0, 0))
                            .build(),
                    SyncRecord.builder()
                            .sceneKey("id:3")
                            .receivedAt(LocalDateTime.of(2022, 1, 1, 0, 0))
                            .sensingTime(LocalDateTime.of(2022, 1, 1, 0, 0))
                            .build()
            ).forEach(syncRecordRepository::save);

            user = persistUser("ROLE_ADMIN");
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "initiatedByMethod",
                "sceneKey",
                "eventName",
                "resultCode",
        })
        public void prefixFields(String field) throws Exception {
            verify(field, "foo", 1);
            verify(field, "fo", 1);
            verify(field, "foobar", 0);
            verify(field, "bar", 0);
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "productName",
        })
        public void exactMatchFields(String field) throws Exception {
            verify(field, "foo", 1);
            verify(field, "fo", 0);
            verify(field, "foobar", 0);
            verify(field, "bar", 0);
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "receivedAt",
                "sensingTime",
        })
        public void rangeFields(String field) throws Exception {
            mockMvc.perform(get(URL)
                    .param(field + "From", "2021-01-01T00:00:00.000Z")
                    .param("sort", field + ",asc")
                    .with(jwtBearerToken(user, objectMapper)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(2)))
                    .andExpect(jsonPath("$.content[0].sceneKey").value("id:2"))
                    .andExpect(jsonPath("$.content[1].sceneKey").value("id:3"));
            mockMvc.perform(get(URL)
                    .param(field + "From", "2021-01-01T00:00:00.000Z")
                    .param("sort", field + ",desc")
                    .with(jwtBearerToken(user, objectMapper)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(2)))
                    .andExpect(jsonPath("$.content[0].sceneKey").value("id:3"))
                    .andExpect(jsonPath("$.content[1].sceneKey").value("id:2"));
            mockMvc.perform(get(URL)
                    .param(field + "From", "2021-01-01T00:00:00.000Z")
                    .param(field + "To", "2022-01-01T00:00:00.000Z")
                    .with(jwtBearerToken(user, objectMapper)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(1)))
                    .andExpect(jsonPath("$.content[0].sceneKey").value("id:2"));
            mockMvc.perform(get(URL)
                    .param(field + "To", "2022-01-01T00:00:00.000Z")
                    .param("sort", field + ",asc")
                    .with(jwtBearerToken(user, objectMapper)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(2)))
                    .andExpect(jsonPath("$.content[0].sceneKey").value("id:1"))
                    .andExpect(jsonPath("$.content[1].sceneKey").value("id:2"));
            mockMvc.perform(get(URL)
                    .param(field + "To", "2022-01-01T00:00:00.000Z")
                    .param("sort", field + ",desc")
                    .with(jwtBearerToken(user, objectMapper)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(2)))
                    .andExpect(jsonPath("$.content[0].sceneKey").value("id:2"))
                    .andExpect(jsonPath("$.content[1].sceneKey").value("id:1"));
        }

        @ParameterizedTest
        @CsvSource({
                "receivedAt",
                "sensingTime",
                "exceptionMessage",
                "parameters",
                "non-existent",
        })
        public void shouldTolerateIgnoredAndNonExistentParams(String paramName) throws Exception {
            mockMvc.perform(get(URL)
                    .param(paramName, "foo")
                    .with(jwtBearerToken(user, objectMapper)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(4)));
        }

        @Test
        public void shouldDelete() throws Exception {
            assertThat(syncRecordRepository.count(), is(4L));

            mockMvc.perform(delete(URL)
                    .with(jwtBearerToken(user, objectMapper)))
                    .andExpect(status().isNoContent());

            assertThat(syncRecordRepository.count(), is(0L));
        }

        private void verify(String field, String search, int size) throws Exception {
            mockMvc.perform(get(URL)
                    .param(field, search)
                    .with(jwtBearerToken(user, objectMapper)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(size)));
        }
    }
}
