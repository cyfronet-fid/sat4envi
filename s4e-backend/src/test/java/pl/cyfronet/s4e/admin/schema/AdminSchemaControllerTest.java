package pl.cyfronet.s4e.admin.schema;

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
import pl.cyfronet.s4e.BasicTest;
import pl.cyfronet.s4e.TestDbHelper;
import pl.cyfronet.s4e.TestResourceHelper;
import pl.cyfronet.s4e.bean.AppUser;
import pl.cyfronet.s4e.bean.Schema;
import pl.cyfronet.s4e.data.repository.AppUserRepository;
import pl.cyfronet.s4e.data.repository.SchemaRepository;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static pl.cyfronet.s4e.Constants.ADMIN_PREFIX;
import static pl.cyfronet.s4e.TestJwtUtil.jwtBearerToken;

@AutoConfigureMockMvc
@BasicTest
@Slf4j
public class AdminSchemaControllerTest {
    private static final String S1_SCENE_SCHEMA_PATH = "classpath:schema/Sentinel-1.scene.v1.json";
    private static final String S2_SCENE_SCHEMA_PATH = "classpath:schema/Sentinel-2.scene.v1.json";

    private Faker faker = new Faker();

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TestDbHelper testDbHelper;

    @Autowired
    private TestResourceHelper testResourceHelper;

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

    @Autowired
    private SchemaRepository schemaRepository;

    private AdminCreateSchemaRequest.AdminCreateSchemaRequestBuilder getCreateSchemaRequestBuilder() {
        return AdminCreateSchemaRequest.builder()
                .name("test.scene.v1.json")
                .type(Schema.Type.SCENE)
                .content(new String(testResourceHelper.getAsBytes(S1_SCENE_SCHEMA_PATH)));
    }

    @Nested
    class Create {
        @Test
        public void shouldWork() throws Exception {
            AdminCreateSchemaRequest request = getCreateSchemaRequestBuilder().build();

            assertThat(schemaRepository.count(), is(equalTo(0L)));

            mockMvc.perform(post(ADMIN_PREFIX + "/schemas")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsBytes(request))
                    .with(jwtBearerToken(admin, objectMapper)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name", equalTo(request.getName())));

            assertThat(schemaRepository.count(), is(equalTo(1L)));
        }

        @Test
        public void shouldForbidNonAdmin() throws Exception {
            AdminCreateSchemaRequest request = getCreateSchemaRequestBuilder().build();

            assertThat(schemaRepository.count(), is(equalTo(0L)));

            mockMvc.perform(post(ADMIN_PREFIX + "/schemas")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsBytes(request))
                    .with(jwtBearerToken(user, objectMapper)))
                    .andExpect(status().isForbidden());

            assertThat(schemaRepository.count(), is(equalTo(0L)));
        }

        @Test
        public void shouldForbidUnauthorized() throws Exception {
            AdminCreateSchemaRequest request = getCreateSchemaRequestBuilder().build();

            assertThat(schemaRepository.count(), is(equalTo(0L)));

            mockMvc.perform(post(ADMIN_PREFIX + "/schemas")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsBytes(request)))
                    .andExpect(status().isUnauthorized());

            assertThat(schemaRepository.count(), is(equalTo(0L)));
        }

        @Test
        public void shouldSetPrevious() throws Exception {
            schemaRepository.save(Schema.builder()
                    .name("test.scene.v1.json")
                    .type(Schema.Type.SCENE)
                    .content(new String(testResourceHelper.getAsBytes(S1_SCENE_SCHEMA_PATH)))
                    .build());

            AdminCreateSchemaRequest request = getCreateSchemaRequestBuilder()
                    .name("test.scene.v2.json")
                    .previous("test.scene.v1.json")
                    .build();

            assertThat(schemaRepository.count(), is(equalTo(1L)));

            mockMvc.perform(post(ADMIN_PREFIX + "/schemas")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsBytes(request))
                    .with(jwtBearerToken(admin, objectMapper)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name", equalTo(request.getName())));

            assertThat(schemaRepository.count(), is(equalTo(2L)));
        }

        @Test
        public void shouldVerifySchemaType() throws Exception {
            schemaRepository.save(Schema.builder()
                    .name("test.metadata.v1.json")
                    .type(Schema.Type.METADATA)
                    .content(new String(testResourceHelper.getAsBytes(S1_SCENE_SCHEMA_PATH)))
                    .build());

            AdminCreateSchemaRequest request = getCreateSchemaRequestBuilder()
                    .name("test.scene.v2.json")
                    .previous("test.metadata.v1.json")
                    .build();

            assertThat(schemaRepository.count(), is(equalTo(1L)));

            mockMvc.perform(post(ADMIN_PREFIX + "/schemas")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsBytes(request))
                    .with(jwtBearerToken(admin, objectMapper)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.__general__", not(blankOrNullString())));

            assertThat(schemaRepository.count(), is(equalTo(1L)));
        }

        @Test
        public void shouldVerifySchemaContentIsJson() throws Exception {
            AdminCreateSchemaRequest request = getCreateSchemaRequestBuilder()
                    .content("{\"key\":sth_wrong_with_this_value}")
                    .build();

            assertThat(schemaRepository.count(), is(equalTo(0L)));

            mockMvc.perform(post(ADMIN_PREFIX + "/schemas")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsBytes(request))
                    .with(jwtBearerToken(admin, objectMapper)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.content[0]", containsString("JSON")));

            assertThat(schemaRepository.count(), is(equalTo(0L)));
        }
    }

    private void createSchemas() {
        String content = new String(testResourceHelper.getAsBytes(S1_SCENE_SCHEMA_PATH));
        Schema testSceneV1 = schemaRepository.save(Schema.builder()
                .name("test.scene.v1.json")
                .type(Schema.Type.SCENE)
                .content(content)
                .build());
        schemaRepository.save(Schema.builder()
                .name("test.scene.v2.json")
                .type(Schema.Type.SCENE)
                .content(content)
                .previous(testSceneV1)
                .build());
        schemaRepository.save(Schema.builder()
                .name("abc.metadata.v1.json")
                .type(Schema.Type.METADATA)
                .content(content)
                .build());
    }

    @Nested
    class ReadAndList {
        @BeforeEach
        public void beforeEach() {
            createSchemas();
        }

        @Test
        public void shouldList() throws Exception {
            assertThat(schemaRepository.count(), is(equalTo(3L)));

            mockMvc.perform(get(ADMIN_PREFIX + "/schemas")
                    .with(jwtBearerToken(admin, objectMapper)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()", is(equalTo(3))))
                    .andExpect(jsonPath("$[0].name", is(equalTo("abc.metadata.v1.json"))))
                    .andExpect(jsonPath("$[2].previous.name", is(equalTo("test.scene.v1.json"))));
        }

        @Test
        public void shouldRead() throws Exception {
            assertThat(schemaRepository.count(), is(equalTo(3L)));

            mockMvc.perform(get(ADMIN_PREFIX + "/schemas/{name}", "test.scene.v1.json")
                    .with(jwtBearerToken(admin, objectMapper)))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    class Update {
        @BeforeEach
        public void beforeEach() {
            createSchemas();
        }

        @Test
        public void shouldWork() throws Exception {
            String content = new String(testResourceHelper.getAsBytes(S2_SCENE_SCHEMA_PATH));
            AdminUpdateSchemaRequest request = AdminUpdateSchemaRequest.builder()
                    .type(Schema.Type.METADATA)
                    .content(content)
                    .previous("abc.metadata.v1.json")
                    .build();

            assertThat(schemaRepository.count(), is(equalTo(3L)));

            mockMvc.perform(put(ADMIN_PREFIX + "/schemas/{name}", "test.scene.v1.json")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsBytes(request))
                    .with(jwtBearerToken(admin, objectMapper)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.type", equalTo(request.getType().name())))
                    .andExpect(jsonPath("$.content", equalTo(content)))
                    .andExpect(jsonPath("$.previous.name", equalTo("abc.metadata.v1.json")));

            assertThat(schemaRepository.count(), is(equalTo(3L)));
        }
    }

    @Nested
    class Delete {
        @BeforeEach
        public void beforeEach() {
            createSchemas();
        }

        @Test
        public void shouldDeleteSchema() throws Exception {
            assertThat(schemaRepository.count(), is(equalTo(3L)));

            mockMvc.perform(delete(ADMIN_PREFIX + "/schemas/{name}", "test.scene.v2.json")
                    .with(jwtBearerToken(admin, objectMapper)))
                    .andExpect(status().isOk());

            assertThat(schemaRepository.count(), is(equalTo(2L)));
        }

        @Test
        public void shouldntDeleteSchemaIfItHasChildren() throws Exception {
            assertThat(schemaRepository.count(), is(equalTo(3L)));

            mockMvc.perform(delete(ADMIN_PREFIX + "/schemas/{name}", "test.scene.v1.json")
                    .with(jwtBearerToken(admin, objectMapper)))
                    .andExpect(status().isBadRequest());

            assertThat(schemaRepository.count(), is(equalTo(3L)));
        }

        @Test
        public void shouldReturn404IfDeletingNonExistingSchema() throws Exception {
            assertThat(schemaRepository.count(), is(equalTo(3L)));

            mockMvc.perform(delete(ADMIN_PREFIX + "/schemas/{name}", "doesnt_exist.scene.v1.json")
                    .with(jwtBearerToken(admin, objectMapper)))
                    .andExpect(status().isNotFound());

            assertThat(schemaRepository.count(), is(equalTo(3L)));
        }
    }
}
