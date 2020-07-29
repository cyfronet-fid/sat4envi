package pl.cyfronet.s4e.admin.sync;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javafaker.Faker;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import pl.cyfronet.s4e.IntegrationTest;
import pl.cyfronet.s4e.TestDbHelper;
import pl.cyfronet.s4e.bean.AppUser;
import pl.cyfronet.s4e.data.repository.AppUserRepository;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static pl.cyfronet.s4e.Constants.ADMIN_PREFIX;
import static pl.cyfronet.s4e.TestJwtUtil.jwtBearerToken;

@AutoConfigureMockMvc
@IntegrationTest
@Slf4j
public class AdminSceneSyncControllerTest {
    private Faker faker = new Faker();

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TestDbHelper testDbHelper;
    private AppUser user;
    private AppUser admin;

    @BeforeEach
    public void beforeEach() {
        testDbHelper.clean();

        user = appUserRepository.save(AppUser.builder()
                .email(faker.internet().emailAddress("user"))
                .name(faker.name().firstName())
                .surname(faker.name().lastName())
                .password("{noop}" + faker.internet().password())
                .enabled(true)
                .build());

        admin = appUserRepository.save(AppUser.builder()
                .email(faker.internet().emailAddress("admin"))
                .name(faker.name().firstName())
                .surname(faker.name().lastName())
                .password("{noop}" + faker.internet().password())
                .enabled(true)
                .admin(true)
                .build());
    }

    @AfterEach
    public void afterEach() {
        testDbHelper.clean();
    }

    @Nested
    class Sync {
        @Test
        public void shouldWork() throws Exception {
            // there is no .scene files int s4e-test-1
            AdminSceneSyncRequest request = AdminSceneSyncRequest.builder().prefix("/").build();
            mockMvc.perform(post(ADMIN_PREFIX + "/sync")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsBytes(request))
                    .with(jwtBearerToken(admin, objectMapper)))
                    .andExpect(status().isOk());
        }

        @Test
        public void shouldValidateRequest() throws Exception {
            AdminSceneSyncRequest request = AdminSceneSyncRequest.builder().build();
            mockMvc.perform(post(ADMIN_PREFIX + "/sync")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsBytes(request))
                    .with(jwtBearerToken(admin, objectMapper)))
                    .andExpect(status().isBadRequest());
        }
        @Test
        public void shouldForbidNonAdmin() throws Exception {
            AdminSceneSyncRequest request = AdminSceneSyncRequest.builder().prefix("prefix").build();
            mockMvc.perform(post(ADMIN_PREFIX + "/sync")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsBytes(request))
                    .with(jwtBearerToken(user, objectMapper)))
                    .andExpect(status().isForbidden());
        }

        @Test
        public void shouldForbidUnauthorized() throws Exception {
            AdminSceneSyncRequest request = AdminSceneSyncRequest.builder().prefix("prefix").build();
            mockMvc.perform(post(ADMIN_PREFIX + "/sync")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsBytes(request)))
                    .andExpect(status().isUnauthorized());
        }
    }
}
