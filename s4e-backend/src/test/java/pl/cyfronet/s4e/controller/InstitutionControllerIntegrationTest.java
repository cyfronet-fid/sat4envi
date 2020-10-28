package pl.cyfronet.s4e.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import pl.cyfronet.s4e.IntegrationTest;
import pl.cyfronet.s4e.TestDbHelper;
import pl.cyfronet.s4e.TestResourceHelper;
import pl.cyfronet.s4e.bean.AppRole;
import pl.cyfronet.s4e.bean.AppUser;
import pl.cyfronet.s4e.bean.Institution;
import pl.cyfronet.s4e.bean.UserRole;
import pl.cyfronet.s4e.controller.request.CreateChildInstitutionRequest;
import pl.cyfronet.s4e.controller.request.CreateInstitutionRequest;
import pl.cyfronet.s4e.controller.request.UpdateInstitutionRequest;
import pl.cyfronet.s4e.data.repository.AppUserRepository;
import pl.cyfronet.s4e.data.repository.InstitutionRepository;
import pl.cyfronet.s4e.data.repository.UserRoleRepository;
import pl.cyfronet.s4e.properties.FileStorageProperties;
import pl.cyfronet.s4e.service.SlugService;
import pl.cyfronet.s4e.service.UserRoleService;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static pl.cyfronet.s4e.Constants.API_PREFIX_V1;
import static pl.cyfronet.s4e.TestJwtUtil.jwtBearerToken;

@AutoConfigureMockMvc
@IntegrationTest
@Slf4j
public class InstitutionControllerIntegrationTest {
    private static final String IMAGE_PNG_PATH = "classpath:images/emblem.png";
    public static final String PROFILE_EMAIL = "get@profile.com";
    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private InstitutionRepository institutionRepository;

    @Autowired
    private UserRoleRepository userRoleRepository;

    @Autowired
    private UserRoleService userRoleService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TestDbHelper testDbHelper;

    @Autowired
    private S3Client s3Client;

    @Autowired
    private FileStorageProperties fileStorageProperties;

    @Autowired
    private SlugService slugService;

    @Autowired
    private TestResourceHelper testResourceHelper;

    private AppUser appUser;
    private String parentSlug;
    private String testInstitution = "Test Institution - ZKPL";

    @BeforeEach
    public void beforeEach() {
        testDbHelper.clean();

        appUser = appUserRepository.save(AppUser.builder()
                .email(PROFILE_EMAIL)
                .name("Get")
                .surname("Profile")
                .password("{noop}password")
                .admin(true)
                .enabled(true)
                .build());
        //1st lvl institution
        parentSlug = slugService.slugify(testInstitution);
        Institution institution = institutionRepository.save(Institution.builder()
                .name(testInstitution)
                .slug(parentSlug)
                .build());

        UserRole userRole = UserRole.builder().role(AppRole.INST_MEMBER).user(appUser).institution(institution).build();
        userRoleRepository.save(userRole);
    }

    @AfterEach
    public void afterEach() {
        testDbHelper.clean();
    }

    @Test
    public void shouldCreateInstitutionWithEmblem() throws Exception {
        CreateInstitutionRequest request = CreateInstitutionRequest.builder()
                .name("Test Institution ZK")
                .emblem(testResourceHelper.getAsStringInBase64(IMAGE_PNG_PATH))
                .build();

        mockMvc.perform(post(API_PREFIX_V1 + "/institutions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(request))
                .with(jwtBearerToken(appUser, objectMapper)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is(equalTo(request.getName()))))
                .andExpect(jsonPath("$.slug", is(equalTo(slugService.slugify(request.getName())))));

        // Expect object is found. Otherwise, throw NoSuchKeyException.
        assertDoesNotThrow(() -> s3Client.getObject(
                GetObjectRequest.builder()
                        .bucket(fileStorageProperties.getBucket())
                        .key(fileStorageProperties.getKeyPrefixEmblem() + slugService.slugify(request.getName()))
                        .build()).response());
    }

    public void shouldntCreateInstitutionWithoutEmblem() throws Exception {
        // TODO: this test should be used when emblem validation is fixed
        CreateInstitutionRequest request = CreateInstitutionRequest.builder()
                .name("Test Institution ZK")
                .build();

        mockMvc.perform(post(API_PREFIX_V1 + "/institutions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(request))
                .with(jwtBearerToken(appUser, objectMapper)))
                .andExpect(status().is5xxServerError());

        // Expect object is not found.
        assertThrows(NoSuchKeyException.class, () -> s3Client.getObject(
                GetObjectRequest.builder()
                        .bucket(fileStorageProperties.getBucket())
                        .key(fileStorageProperties.getKeyPrefixEmblem() + "test-institution-zk")
                        .build()).response());
    }

    @Test
    public void shouldUpdateInstitutionWithEmblem() throws Exception {
        CreateInstitutionRequest request = CreateInstitutionRequest.builder()
                .name("Test Institution ZK")
                .emblem(testResourceHelper.getAsStringInBase64(IMAGE_PNG_PATH))
                .build();
        String slugInstitution = slugService.slugify(request.getName());

        mockMvc.perform(post(API_PREFIX_V1 + "/institutions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(request))
                .with(jwtBearerToken(appUser, objectMapper)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is(equalTo(request.getName()))))
                .andExpect(jsonPath("$.slug", is(equalTo(slugService.slugify(request.getName())))));

        // Expect object is found. Otherwise, throw NoSuchKeyException.
        assertDoesNotThrow(() -> s3Client.getObject(
                GetObjectRequest.builder()
                        .bucket(fileStorageProperties.getBucket())
                        .key(fileStorageProperties.getKeyPrefixEmblem() + slugInstitution)
                        .build()).response());
        // update
        UpdateInstitutionRequest updateRequest = UpdateInstitutionRequest.builder()
                .name("Test Institution PAK")
                .emblem(testResourceHelper.getAsStringInBase64(IMAGE_PNG_PATH))
                .build();
        String updateSlug = slugService.slugify(updateRequest.getName());

        mockMvc.perform(put(API_PREFIX_V1 + "/institutions/{institution}", slugInstitution)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(updateRequest))
                .with(jwtBearerToken(appUser, objectMapper)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is(equalTo(updateRequest.getName()))))
                .andExpect(jsonPath("$.slug", is(equalTo(updateSlug))));

        // Expect object is found. Otherwise, throw NoSuchKeyException.
        assertDoesNotThrow(() -> s3Client.getObject(
                GetObjectRequest.builder()
                        .bucket(fileStorageProperties.getBucket())
                        .key(fileStorageProperties.getKeyPrefixEmblem() + updateSlug)
                        .build()).response());
        // Expect object is not found.
        assertThrows(NoSuchKeyException.class, () -> s3Client.getObject(
                GetObjectRequest.builder()
                        .bucket(fileStorageProperties.getBucket())
                        .key(fileStorageProperties.getKeyPrefixEmblem() + slugInstitution)
                        .build()).response());
    }

    @Test
    public void shouldCreateChildInstitutionWithEmblem() throws Exception {
        CreateChildInstitutionRequest request = CreateChildInstitutionRequest.builder()
                .name("Test Institution ZK")
                .emblem(testResourceHelper.getAsStringInBase64(IMAGE_PNG_PATH))
                .build();
        String slugInstitution = slugService.slugify(request.getName());

        mockMvc.perform(post(API_PREFIX_V1 + "/institutions/{institution}/child", parentSlug)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(request))
                .with(jwtBearerToken(appUser, objectMapper)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is(equalTo(request.getName()))))
                .andExpect(jsonPath("$.slug", is(equalTo(slugService.slugify(request.getName())))));

        // Expect object is found. Otherwise, throw NoSuchKeyException.
        assertDoesNotThrow(() -> s3Client.getObject(
                GetObjectRequest.builder()
                        .bucket(fileStorageProperties.getBucket())
                        .key(fileStorageProperties.getKeyPrefixEmblem() + slugInstitution)
                        .build()).response());
    }

    @Test
    public void shouldUpdateInstitution() throws Exception {
        String test = "test";
        UpdateInstitutionRequest request = UpdateInstitutionRequest.builder()
                .name(testInstitution)
                .emblem(testResourceHelper.getAsStringInBase64(IMAGE_PNG_PATH))
                .address(test)
                .city(test)
                .postalCode(test)
                .phone(test)
                .secondaryPhone(test)

                .build();

        mockMvc.perform(put(API_PREFIX_V1 + "/institutions/{institution}", parentSlug)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(request))
                .with(jwtBearerToken(appUser, objectMapper)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is(equalTo(request.getName()))))
                .andExpect(jsonPath("$.slug", is(equalTo(slugService.slugify(request.getName())))));

        mockMvc.perform(get(API_PREFIX_V1 + "/institutions/{institution}", parentSlug)
                .contentType(MediaType.APPLICATION_JSON)
                .with(jwtBearerToken(appUser, objectMapper)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(is(equalTo(testInstitution))))
                .andExpect(jsonPath("$.address").value(is(equalTo(test))))
                .andExpect(jsonPath("$.city").value(is(equalTo(test))))
                .andExpect(jsonPath("$.postalCode").value(is(equalTo(test))))
                .andExpect(jsonPath("$.phone").value(is(equalTo(test))))
                .andExpect(jsonPath("$.secondaryPhone").value(is(equalTo(test))))
                .andExpect(jsonPath("$.slug").value(is(equalTo(parentSlug))));
    }
}
