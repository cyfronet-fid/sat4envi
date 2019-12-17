package pl.cyfronet.s4e.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import pl.cyfronet.s4e.FileStorageProperties;
import pl.cyfronet.s4e.IntegrationTest;
import pl.cyfronet.s4e.TestDbHelper;
import pl.cyfronet.s4e.TestResourceHelper;
import pl.cyfronet.s4e.bean.AppUser;
import pl.cyfronet.s4e.bean.SavedView;
import pl.cyfronet.s4e.controller.request.CreateSavedViewRequest;
import pl.cyfronet.s4e.data.repository.AppUserRepository;
import pl.cyfronet.s4e.data.repository.SavedViewRepository;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static pl.cyfronet.s4e.Constants.API_PREFIX_V1;
import static pl.cyfronet.s4e.TestJwtUtil.jwtBearerToken;

@AutoConfigureMockMvc
@IntegrationTest
@Slf4j
public class SavedViewControllerIntegrationTest {
    private static final Pattern UUID_PATTERN = Pattern.compile("[0-9A-Za-z]{8}-[0-9A-Za-z]{4}-[0-9A-Za-z]{4}-[0-9A-Za-z]{4}-[0-9A-Za-z]{12}");
    private static final String IMAGE_PNG_PATH = "classpath:images/image.png";

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private SavedViewRepository savedViewRepository;

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
    private TestResourceHelper testResourceHelper;

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

    @AfterEach
    public void afterEach() {
        testDbHelper.clean();
    }

    public interface SavedViewProjection {
        UUID getId();
        LocalDateTime getCreatedAt();
        OwnerProjection getOwner();
    }

    public interface OwnerProjection {
        String getEmail();
    }

    @Test
    public void shouldCreateSavedView() throws Exception {
        CreateSavedViewRequest request = CreateSavedViewRequest.builder()
                .caption("view caption")
                .thumbnail(testResourceHelper.getAsStringInBase64(IMAGE_PNG_PATH))
                .configuration(Map.of("key-1", "val-1", "key-2", 25, "key-3", Map.of("key-3-1", true)))
                .build();

        assertThat(savedViewRepository.count(), is(equalTo(0L)));

        mockMvc.perform(post(API_PREFIX_V1 + "/savedViews")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(request))
                .with(jwtBearerToken(appUser, objectMapper)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.uuid", matchesPattern(UUID_PATTERN)));

        val allSavedViews = savedViewRepository.findAllBy(SavedViewProjection.class);

        assertThat(allSavedViews, hasSize(1));
        val savedView = allSavedViews.get(0);
        assertThat(savedView.getCreatedAt(), is(lessThan(LocalDateTime.now())));
        assertThat(savedView.getCreatedAt(), is(greaterThan(LocalDateTime.now().minusSeconds(1))));
        assertThat(savedView.getOwner().getEmail(), is(equalTo("get@profile.com")));

        // Expect object is found. Otherwise, throw NoSuchKeyException.
        assertDoesNotThrow(() -> s3Client.getObject(
                GetObjectRequest.builder()
                        .bucket(fileStorageProperties.getBucket())
                        .key(fileStorageProperties.getKeyPrefix() + savedView.getId())
                        .build()).response());
    }

    @Test
    public void shouldDeleteSavedView() throws Exception {
        SavedView savedView = savedViewRepository.save(SavedView.builder()
                .owner(appUser)
                .createdAt(LocalDateTime.of(2010, 1, 1, 0, 0))
                .caption("caption-1")
                .configuration(Map.of("key-1", "val-1"))
                .build());

        s3Client.putObject(
                PutObjectRequest.builder()
                        .bucket(fileStorageProperties.getBucket())
                        .key(fileStorageProperties.getKeyPrefix() + savedView.getId())
                        .contentType("image/jpeg")
                        .build(),
                RequestBody.fromBytes(testResourceHelper.getAsBytes(IMAGE_PNG_PATH)));

        assertThat(savedViewRepository.count(), is(equalTo(1L)));

        mockMvc.perform(delete(API_PREFIX_V1 + "/savedViews/{uuid}", savedView.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .with(jwtBearerToken(appUser, objectMapper)))
                .andExpect(status().isOk());

        assertThat(savedViewRepository.count(), is(equalTo(0L)));

        assertThrows(NoSuchKeyException.class, () ->
                s3Client.getObject(GetObjectRequest.builder()
                        .bucket(fileStorageProperties.getBucket())
                        .key(fileStorageProperties.getKeyPrefix() + savedView.getId())
                        .build()).response());
    }
}
