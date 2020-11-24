package pl.cyfronet.s4e.admin.sync.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javafaker.Faker;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import pl.cyfronet.s4e.IntegrationTest;
import pl.cyfronet.s4e.TestDbHelper;
import pl.cyfronet.s4e.admin.sync.task.SyncJob;
import pl.cyfronet.s4e.admin.sync.task.SyncJobStore;
import pl.cyfronet.s4e.bean.AppUser;
import pl.cyfronet.s4e.data.repository.AppUserRepository;
import pl.cyfronet.s4e.sync.Error;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static pl.cyfronet.s4e.Constants.ADMIN_PREFIX;
import static pl.cyfronet.s4e.TestJwtUtil.jwtBearerToken;

@AutoConfigureMockMvc
@IntegrationTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestPropertySource(properties = {
        "s3.bucket=scene-acceptor-test"
})
@Slf4j
public class AdminSyncJobControllerIntegrationTest {
    private final Faker faker = new Faker();

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SyncJobStore syncJobStore;

    @Autowired
    private TestDbHelper testDbHelper;

    @Autowired
    private MockMvc mockMvc;

    private AppUser user;
    private AppUser admin;

    @BeforeEach
    public void beforeAll() {
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
                .authority("ROLE_ADMIN")
                .build());
    }

    @Test
    @Order(1)
    public void ensureMethodsAreSecured() throws Exception {
        mockMvc.perform(get(ADMIN_PREFIX + "/sync-jobs")
                .with(jwtBearerToken(user, objectMapper)))
                .andExpect(status().isForbidden());

        mockMvc.perform(get(ADMIN_PREFIX + "/sync-jobs"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Order(2)
    public void createJob() throws Exception {
        CreateSyncJobRequest request = CreateSyncJobRequest.builder()
                .name("test-1")
                .prefix("Sentinel-1/")
                .build();

        mockMvc.perform(put(ADMIN_PREFIX + "/sync-jobs")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(request))
                .with(jwtBearerToken(admin, objectMapper)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.state", is(equalTo("PENDING"))))
                .andExpect(jsonPath("$.stateHistory[0].sceneCount", is(equalTo(1))));
    }

    @Test
    @Order(3)
    public void runJob() throws Exception {
        mockMvc.perform(post(ADMIN_PREFIX + "/sync-jobs/{name}/run", "test-1")
                .with(jwtBearerToken(admin, objectMapper)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.state", is(equalTo("RUNNING"))))
                .andExpect(jsonPath("$.stateHistory[1].state", is(equalTo("RUNNING"))));
    }

    @Test
    @Order(4)
    public void listJobs() throws Exception {
        mockMvc.perform(get(ADMIN_PREFIX + "/sync-jobs")
                .with(jwtBearerToken(admin, objectMapper)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    @Order(5)
    public void showJob() throws Exception {
        SyncJob syncJob = syncJobStore.find("test-1").get();
        await().until(syncJob::getState, is(equalTo(SyncJob.State.FINISHED)));

        mockMvc.perform(get(ADMIN_PREFIX + "/sync-jobs/{name}", "test-1")
                .with(jwtBearerToken(admin, objectMapper)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stateHistory[1].errorsCount", is(equalTo(1))));
    }

    @Test
    @Order(6)
    public void showErrors() throws Exception {
        mockMvc.perform(get(ADMIN_PREFIX + "/sync-jobs/{name}/errors", "test-1")
                .with(jwtBearerToken(admin, objectMapper)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errors[0].code", is(equalTo(Error.ERR_SCHEMA_NOT_FOUND))));
    }

    @Test
    @Order(7)
    public void deleteJob() throws Exception {
        mockMvc.perform(delete(ADMIN_PREFIX + "/sync-jobs/{name}", "test-1")
                .with(jwtBearerToken(admin, objectMapper)))
                .andExpect(status().isOk());

        assertThat(syncJobStore.list(), hasSize(0));
    }
}
