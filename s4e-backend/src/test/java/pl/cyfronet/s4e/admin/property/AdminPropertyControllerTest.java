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

package pl.cyfronet.s4e.admin.property;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import pl.cyfronet.s4e.BasicTest;
import pl.cyfronet.s4e.Constants;
import pl.cyfronet.s4e.TestDbHelper;
import pl.cyfronet.s4e.bean.AppUser;
import pl.cyfronet.s4e.bean.Property;
import pl.cyfronet.s4e.data.repository.AppUserRepository;
import pl.cyfronet.s4e.data.repository.PropertyRepository;

import static com.github.npathai.hamcrestopt.OptionalMatchers.isPresent;
import static com.github.npathai.hamcrestopt.OptionalMatchers.isPresentAnd;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static pl.cyfronet.s4e.TestJwtUtil.jwtBearerToken;

@BasicTest
@AutoConfigureMockMvc
class AdminPropertyControllerTest {
    private static final String TEST_NAME = "test_name";
    private static final String TEST_VALUE = "test value";

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private PropertyRepository propertyRepository;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TestDbHelper testDbHelper;

    @Autowired
    private ObjectMapper objectMapper;

    private AppUser admin;
    private AppUser user;

    @BeforeEach
    public void beforeEach() {
        testDbHelper.clean();

        admin = appUserRepository.save(AppUser.builder()
                .email("admin@mail.pl")
                .name("John")
                .surname("Smith")
                .password("{noop}password")
                .enabled(true)
                .authority("ROLE_ADMIN")
                .build());

        user = appUserRepository.save(AppUser.builder()
                .email("user@mail.pl")
                .name("Amy")
                .surname("Smith")
                .password("{noop}password")
                .enabled(true)
                .build());
    }

    @Nested
    public class PutEndpoint {
        private final String URL = Constants.ADMIN_PREFIX + "/properties/{name}";

        @Test
        public void shouldCreateProperty() throws Exception {
            AdminPropertyRequest request = AdminPropertyRequest.builder()
                    .value(TEST_VALUE)
                    .build();

            assertThat(propertyRepository.count(), is(equalTo(0L)));
            assertThat(propertyRepository.findByName(TEST_NAME), not(isPresent()));

            mockMvc.perform(put(URL, TEST_NAME)
                    .contentType(MediaType.APPLICATION_JSON)
                    .with(jwtBearerToken(admin, objectMapper))
                    .content(objectMapper.writeValueAsBytes(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name", is(equalTo(TEST_NAME))))
                    .andExpect(jsonPath("$.value", is(equalTo(request.getValue()))));

            assertThat(propertyRepository.findByName(TEST_NAME), isPresentAnd(hasProperty("value", equalTo(request.getValue()))));
        }

        @Test
        public void shouldUpdateProperty() throws Exception {
            AdminPropertyRequest request = AdminPropertyRequest.builder()
                    .value(TEST_VALUE)
                    .build();

            propertyRepository.save(Property.builder()
                    .name(TEST_NAME)
                    .value("previous value")
                    .build());

            assertThat(propertyRepository.findByName(TEST_NAME), isPresentAnd(hasProperty("value", equalTo("previous value"))));

            mockMvc.perform(put(URL, TEST_NAME)
                    .contentType(MediaType.APPLICATION_JSON)
                    .with(jwtBearerToken(admin, objectMapper))
                    .content(objectMapper.writeValueAsBytes(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name", is(equalTo(TEST_NAME))))
                    .andExpect(jsonPath("$.value", is(equalTo(request.getValue()))));

            assertThat(propertyRepository.findByName(TEST_NAME), isPresentAnd(hasProperty("value", equalTo(request.getValue()))));
        }

        @Test
        public void shouldBeSecured() throws Exception {
            assertThat(propertyRepository.count(), is(equalTo(0L)));

            mockMvc.perform(put(URL, TEST_NAME)
                    .with(jwtBearerToken(user, objectMapper)))
                    .andExpect(status().isForbidden());

            assertThat(propertyRepository.count(), is(equalTo(0L)));
        }
    }

    @Nested
    class ListEndpoint {
        private final String URL = Constants.ADMIN_PREFIX + "/properties";

        @BeforeEach
        public void beforeEach() {
            propertyRepository.save(Property.builder()
                    .name(TEST_NAME)
                    .value(TEST_VALUE)
                    .build());
        }

        @Test
        public void shouldWork() throws Exception {
            assertThat(propertyRepository.findByName(TEST_NAME), isPresentAnd(hasProperty("value", equalTo(TEST_VALUE))));

            mockMvc.perform(get(URL)
                    .with(jwtBearerToken(admin, objectMapper)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].name", is(equalTo(TEST_NAME))))
                    .andExpect(jsonPath("$[0].value", is(equalTo(TEST_VALUE))));

            assertThat(propertyRepository.findByName(TEST_NAME), isPresentAnd(hasProperty("value", equalTo(TEST_VALUE))));
        }

        @Test
        public void shouldBeSecured() throws Exception {
            assertThat(propertyRepository.findByName(TEST_NAME), isPresentAnd(hasProperty("value", equalTo(TEST_VALUE))));

            mockMvc.perform(get(URL)
                    .with(jwtBearerToken(user, objectMapper)))
                    .andExpect(status().isForbidden());

            assertThat(propertyRepository.findByName(TEST_NAME), isPresentAnd(hasProperty("value", equalTo(TEST_VALUE))));
        }
    }

    @Nested
    class DeleteEndpoint {
        private static final String URL = Constants.ADMIN_PREFIX + "/properties/{name}";

        @BeforeEach
        public void beforeEach() {
            propertyRepository.save(Property.builder()
                    .name(TEST_NAME)
                    .value(TEST_VALUE)
                    .build());
        }

        @Test
        public void shouldWork() throws Exception {
            assertThat(propertyRepository.findByName(TEST_NAME), isPresentAnd(hasProperty("value", equalTo(TEST_VALUE))));

            mockMvc.perform(delete(URL, TEST_NAME)
                    .with(jwtBearerToken(admin, objectMapper)))
                    .andExpect(status().isOk());

            assertThat(propertyRepository.findByName(TEST_NAME), not(isPresent()));
        }

        @Test
        public void shouldBeSecured() throws Exception {
            assertThat(propertyRepository.findByName(TEST_NAME), isPresentAnd(hasProperty("value", equalTo(TEST_VALUE))));

            mockMvc.perform(delete(URL, TEST_NAME)
                    .with(jwtBearerToken(user, objectMapper)))
                    .andExpect(status().isForbidden());

            assertThat(propertyRepository.findByName(TEST_NAME), isPresentAnd(hasProperty("value", equalTo(TEST_VALUE))));
        }
    }
}
