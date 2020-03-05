package pl.cyfronet.s4e.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.support.TransactionTemplate;
import pl.cyfronet.s4e.BasicTest;
import pl.cyfronet.s4e.properties.FileStorageProperties;
import pl.cyfronet.s4e.TestDbHelper;
import pl.cyfronet.s4e.TestResourceHelper;
import pl.cyfronet.s4e.bean.AppUser;
import pl.cyfronet.s4e.bean.SavedView;
import pl.cyfronet.s4e.controller.request.CreateSavedViewRequest;
import pl.cyfronet.s4e.data.repository.AppUserRepository;
import pl.cyfronet.s4e.data.repository.SavedViewRepository;
import pl.cyfronet.s4e.service.FileStorage;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static pl.cyfronet.s4e.Constants.API_PREFIX_V1;
import static pl.cyfronet.s4e.TestJwtUtil.jwtBearerToken;

@AutoConfigureMockMvc
@BasicTest
@Slf4j
public class SavedViewControllerTest {
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
    private TransactionTemplate transactionTemplate;

    @Autowired
    private FileStorage fileStorage;

    @Autowired
    private FileStorageProperties fileStorageProperties;

    @Autowired
    private TestResourceHelper testResourceHelper;

    private AppUser appUser;

    @BeforeEach
    public void beforeEach() {
        testDbHelper.clean();
        reset(fileStorage);

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
        reset(fileStorage);
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

        mockMvc.perform(post(API_PREFIX_V1 + "/saved-views")
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

        verify(fileStorage).upload(eq(fileStorageProperties.getKeyPrefix() + savedView.getId()), any());
        verifyNoMoreInteractions(fileStorage);
    }

    @Test
    public void shouldntCreateSavedViewIfUserNotLoggedIn() throws Exception {
        CreateSavedViewRequest request = CreateSavedViewRequest.builder()
                .caption("view caption")
                .thumbnail(testResourceHelper.getAsStringInBase64(IMAGE_PNG_PATH))
                .configuration(Map.of("key-1", "val-1", "key-2", 25, "key-3", Map.of("key-3-1", true)))
                .build();

        assertThat(savedViewRepository.count(), is(equalTo(0L)));

        mockMvc.perform(post(API_PREFIX_V1 + "/saved-views")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(request)))
                .andExpect(status().isForbidden());

        assertThat(savedViewRepository.count(), is(equalTo(0L)));
        verifyNoMoreInteractions(fileStorage);
    }

    @Test
    public void shouldntCreateSavedViewIfThumbnailInWrongFormat() throws Exception {
        CreateSavedViewRequest request = CreateSavedViewRequest.builder()
                .caption("view caption")
                .thumbnail("not a base64 encoded image")
                .configuration(Map.of("key-1", "val-1", "key-2", 25, "key-3", Map.of("key-3-1", true)))
                .build();

        assertThat(savedViewRepository.count(), is(equalTo(0L)));

        mockMvc.perform(post(API_PREFIX_V1 + "/saved-views")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.thumbnail", hasSize(3)));

        assertThat(savedViewRepository.count(), is(equalTo(0L)));
        verifyNoMoreInteractions(fileStorage);
    }

    @Test
    public void shouldListSavedViews() throws Exception {
        AppUser otherAppUser = transactionTemplate.execute(status -> {
            val otherAppUserInternal = appUserRepository.save(AppUser.builder()
                    .email("other@user.com")
                    .name("Get")
                    .surname("Profile")
                    .password("{noop}password")
                    .enabled(true)
                    .build());

            savedViewRepository.saveAll(List.of(
                    SavedView.builder()
                            .owner(appUser)
                            .createdAt(LocalDateTime.of(2010, 1, 1, 0, 0))
                            .caption("caption-1")
                            .configuration(Map.of("key-1", "val-1"))
                            .build(),
                    SavedView.builder()
                            .owner(appUser)
                            .createdAt(LocalDateTime.of(2010, 1, 2, 0, 0))
                            .caption("caption-2")
                            .configuration(Map.of("key-1", "val-1"))
                            .build(),
                    SavedView.builder()
                            .owner(otherAppUserInternal)
                            .createdAt(LocalDateTime.of(2010, 1, 2, 0, 0))
                            .caption("caption-3")
                            .configuration(Map.of("key-1", "val-1"))
                            .build()));

            return otherAppUserInternal;
        });

        assertThat(savedViewRepository.count(), is(equalTo(3L)));

        mockMvc.perform(get(API_PREFIX_V1 + "/saved-views")
                .with(jwtBearerToken(appUser, objectMapper)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()", is(equalTo(2))))
                .andExpect(jsonPath("$.content..caption", contains("caption-2", "caption-1")));

        mockMvc.perform(get(API_PREFIX_V1 + "/saved-views")
                .with(jwtBearerToken(otherAppUser, objectMapper)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()", is(equalTo(1))))
                .andExpect(jsonPath("$.content..caption", contains("caption-3")));

        verifyNoInteractions(fileStorage);
    }

    @Test
    public void shouldDeleteSavedView() throws Exception {
        SavedView savedView = savedViewRepository.save(SavedView.builder()
                .owner(appUser)
                .createdAt(LocalDateTime.of(2010, 1, 1, 0, 0))
                .caption("caption-1")
                .configuration(Map.of("key-1", "val-1"))
                .build());

        assertThat(savedViewRepository.count(), is(equalTo(1L)));

        mockMvc.perform(delete(API_PREFIX_V1 + "/saved-views/{uuid}", savedView.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .with(jwtBearerToken(appUser, objectMapper)))
                .andExpect(status().isOk());

        assertThat(savedViewRepository.count(), is(equalTo(0L)));

        verify(fileStorage).delete(fileStorageProperties.getKeyPrefix() + savedView.getId());
        verifyNoMoreInteractions(fileStorage);
    }

    @Test
    public void shouldntDeleteSavedViewOfOtherUser() throws Exception {
        SavedView savedView = transactionTemplate.execute(status -> {
            val otherAppUser = appUserRepository.save(AppUser.builder()
                    .email("other@user.com")
                    .name("Get")
                    .surname("Profile")
                    .password("{noop}password")
                    .enabled(true)
                    .build());

            return savedViewRepository.save(SavedView.builder()
                    .owner(otherAppUser)
                    .createdAt(LocalDateTime.of(2010, 1, 1, 0, 0))
                    .caption("caption-1")
                    .configuration(Map.of("key-1", "val-1"))
                    .build());
        });

        assertThat(savedViewRepository.count(), is(equalTo(1L)));

        mockMvc.perform(delete(API_PREFIX_V1 + "/saved-views/{uuid}", savedView.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .with(jwtBearerToken(appUser, objectMapper)))
                .andExpect(status().isForbidden());

        assertThat(savedViewRepository.count(), is(equalTo(1L)));

        verifyNoInteractions(fileStorage);
    }

    @Test
    public void shouldReturnForbiddenIfDeletingNonExistentSavedView() throws Exception {
        assertThat(savedViewRepository.count(), is(equalTo(0L)));

        mockMvc.perform(delete(API_PREFIX_V1 + "/saved-views/{uuid}", "6dea77af-1de9-4559-be06-0d50b7db1f35")
                .contentType(MediaType.APPLICATION_JSON)
                .with(jwtBearerToken(appUser, objectMapper)))
                .andExpect(status().isForbidden());

        assertThat(savedViewRepository.count(), is(equalTo(0L)));

        verifyNoInteractions(fileStorage);
    }
}
