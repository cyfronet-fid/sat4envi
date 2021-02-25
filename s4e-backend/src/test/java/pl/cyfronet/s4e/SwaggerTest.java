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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import pl.cyfronet.s4e.bean.AppUser;
import pl.cyfronet.s4e.data.repository.AppUserRepository;

import java.util.Arrays;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static pl.cyfronet.s4e.Constants.API_PREFIX_V1;
import static pl.cyfronet.s4e.TestJwtUtil.jwtBearerToken;

@BasicTest
@AutoConfigureMockMvc
public class SwaggerTest {
    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TestDbHelper testDbHelper;

    @BeforeEach
    public void beforeEach() {
        testDbHelper.clean();
    }

    @Test
    public void testPublic() throws Exception {
        mockMvc.perform(get(API_PREFIX_V1 + "/docs/public"))
                .andExpect(status().isOk());
    }

    @Test
    public void testProvider() throws Exception {
        AppUser user = persistUser("OP_SYNC_RECORD_READ");
        mockMvc.perform(get(API_PREFIX_V1 + "/docs/provider")
                .with(jwtBearerToken(user, objectMapper)))
                .andExpect(status().isOk());
    }

    @Test
    public void testPrivate() throws Exception {
        AppUser user = persistUser("ROLE_ADMIN");
        mockMvc.perform(get(API_PREFIX_V1 + "/docs/private")
                .with(jwtBearerToken(user, objectMapper)))
                .andExpect(status().isOk());
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
}
