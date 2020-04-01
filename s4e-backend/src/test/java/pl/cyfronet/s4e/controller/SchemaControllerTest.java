package pl.cyfronet.s4e.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javafaker.Faker;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
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
import pl.cyfronet.s4e.controller.request.CreateSchemaRequest;
import pl.cyfronet.s4e.data.repository.AppUserRepository;
import pl.cyfronet.s4e.data.repository.SchemaRepository;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static pl.cyfronet.s4e.Constants.API_PREFIX_V1;
import static pl.cyfronet.s4e.TestJwtUtil.jwtBearerToken;

@AutoConfigureMockMvc
@BasicTest
@Slf4j
public class SchemaControllerTest {
    private static final String SCENE_SCHEMA_PATH = "classpath:schema/Sentinel-1.scene.v1.json";

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

    private CreateSchemaRequest.CreateSchemaRequestBuilder getCreateSchemaRequestBuilder() {
        return CreateSchemaRequest.builder()
                .name("test.scene.v1.json")
                .type(Schema.Type.SCENE)
                .content(new String(testResourceHelper.getAsBytes(SCENE_SCHEMA_PATH)));
    }

    @Test
    public void shouldCreateSchema() throws Exception {
        CreateSchemaRequest request = getCreateSchemaRequestBuilder().build();

        assertThat(schemaRepository.count(), is(equalTo(0L)));

        mockMvc.perform(post(API_PREFIX_V1 + "/schema")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(request))
                .with(jwtBearerToken(admin, objectMapper)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", equalTo(request.getName())));

        assertThat(schemaRepository.count(), is(equalTo(1L)));
    }

    @Test
    public void shouldntCreateSchemaIfNotAdmin() throws Exception {
        CreateSchemaRequest request = getCreateSchemaRequestBuilder().build();

        assertThat(schemaRepository.count(), is(equalTo(0L)));

        mockMvc.perform(post(API_PREFIX_V1 + "/schema")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(request))
                .with(jwtBearerToken(user, objectMapper)))
                .andExpect(status().isForbidden());

        assertThat(schemaRepository.count(), is(equalTo(0L)));
    }

    @Test
    public void shouldntCreateSchemaIfUnauthorized() throws Exception {
        CreateSchemaRequest request = getCreateSchemaRequestBuilder().build();

        assertThat(schemaRepository.count(), is(equalTo(0L)));

        mockMvc.perform(post(API_PREFIX_V1 + "/schema")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(request)))
                .andExpect(status().isUnauthorized());

        assertThat(schemaRepository.count(), is(equalTo(0L)));
    }

    @Test
    public void shouldCreateSchemaWithPrevious() throws Exception {
        schemaRepository.save(Schema.builder()
                .name("test.scene.v1.json")
                .type(Schema.Type.SCENE)
                .content(new String(testResourceHelper.getAsBytes(SCENE_SCHEMA_PATH)))
                .build());

        CreateSchemaRequest request = getCreateSchemaRequestBuilder()
                .name("test.scene.v2.json")
                .previous("test.scene.v1.json")
                .build();

        assertThat(schemaRepository.count(), is(equalTo(1L)));

        mockMvc.perform(post(API_PREFIX_V1 + "/schema")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(request))
                .with(jwtBearerToken(admin, objectMapper)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", equalTo(request.getName())));

        assertThat(schemaRepository.count(), is(equalTo(2L)));
    }

    @Test
    public void shouldVerifyCreatedSchemaType() throws Exception {
        schemaRepository.save(Schema.builder()
                .name("test.metadata.v1.json")
                .type(Schema.Type.METADATA)
                .content(new String(testResourceHelper.getAsBytes(SCENE_SCHEMA_PATH)))
                .build());

        CreateSchemaRequest request = getCreateSchemaRequestBuilder()
                .name("test.scene.v2.json")
                .previous("test.metadata.v1.json")
                .build();

        assertThat(schemaRepository.count(), is(equalTo(1L)));

        mockMvc.perform(post(API_PREFIX_V1 + "/schema")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(request))
                .with(jwtBearerToken(admin, objectMapper)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.__general__", not(blankOrNullString())));

        assertThat(schemaRepository.count(), is(equalTo(1L)));
    }

    @Test
    public void shouldVerifyCreatedSchemaContentIsJson() throws Exception {
        CreateSchemaRequest request = getCreateSchemaRequestBuilder()
                .content("{\"key\":sth_wrong_with_this_value}")
                .build();

        assertThat(schemaRepository.count(), is(equalTo(0L)));

        mockMvc.perform(post(API_PREFIX_V1 + "/schema")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(request))
                .with(jwtBearerToken(admin, objectMapper)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.content[0]", containsString("JSON")));

        assertThat(schemaRepository.count(), is(equalTo(0L)));
    }

    private void createSchemas() {
        String content = new String(testResourceHelper.getAsBytes(SCENE_SCHEMA_PATH));
        Schema testSceneV1 = schemaRepository.save(Schema.builder()
                .name("test.scene.v1.json")
                .type(Schema.Type.SCENE)
                .content(content)
                .build());
        Schema testSceneV2 = schemaRepository.save(Schema.builder()
                .name("test.scene.v2.json")
                .type(Schema.Type.SCENE)
                .content(content)
                .previous(testSceneV1)
                .build());
        Schema abcMetadataV1 = schemaRepository.save(Schema.builder()
                .name("abc.metadata.v1.json")
                .type(Schema.Type.METADATA)
                .content(content)
                .build());
    }

    @Test
    public void shouldListSchemas() throws Exception {
        createSchemas();

        assertThat(schemaRepository.count(), is(equalTo(3L)));

        mockMvc.perform(get(API_PREFIX_V1 + "/schema"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(equalTo(3))))
                .andExpect(jsonPath("$[0].name", is(equalTo("abc.metadata.v1.json"))))
                .andExpect(jsonPath("$[2].previous.name", is(equalTo("test.scene.v1.json"))));
    }

    @Test
    public void shouldReturnSchema() throws Exception {
        createSchemas();

        assertThat(schemaRepository.count(), is(equalTo(3L)));

        mockMvc.perform(get(API_PREFIX_V1 + "/schema/{name}", "test.scene.v1.json"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(new String(testResourceHelper.getAsBytes(SCENE_SCHEMA_PATH))));
    }

    @Test
    public void shouldDeleteSchema() throws Exception {
        createSchemas();

        assertThat(schemaRepository.count(), is(equalTo(3L)));

        mockMvc.perform(delete(API_PREFIX_V1 + "/schema/{name}", "test.scene.v2.json")
                .with(jwtBearerToken(admin, objectMapper)))
                .andExpect(status().isOk());

        assertThat(schemaRepository.count(), is(equalTo(2L)));
    }

    @Test
    public void shouldntDeleteSchemaIfItHasChildren() throws Exception {
        createSchemas();

        assertThat(schemaRepository.count(), is(equalTo(3L)));

        mockMvc.perform(delete(API_PREFIX_V1 + "/schema/{name}", "test.scene.v1.json")
                .with(jwtBearerToken(admin, objectMapper)))
                .andExpect(status().isBadRequest());

        assertThat(schemaRepository.count(), is(equalTo(3L)));
    }

    @Test
    public void shouldReturn404IfDeletingNonExistingSchema() throws Exception {
        createSchemas();

        assertThat(schemaRepository.count(), is(equalTo(3L)));

        mockMvc.perform(delete(API_PREFIX_V1 + "/schema/{name}", "doesnt_exist.scene.v1.json")
                .with(jwtBearerToken(admin, objectMapper)))
                .andExpect(status().isNotFound());

        assertThat(schemaRepository.count(), is(equalTo(3L)));
    }
}
