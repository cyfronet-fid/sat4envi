package pl.cyfronet.s4e.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import pl.cyfronet.s4e.BasicTest;
import pl.cyfronet.s4e.TestDbHelper;
import pl.cyfronet.s4e.bean.AppUser;
import pl.cyfronet.s4e.bean.SavedView;
import pl.cyfronet.s4e.controller.request.CreateSavedViewRequest;
import pl.cyfronet.s4e.data.repository.AppUserRepository;
import pl.cyfronet.s4e.data.repository.SavedViewRepository;
import pl.cyfronet.s4e.security.AppUserDetails;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static pl.cyfronet.s4e.Constants.API_PREFIX_V1;
import static pl.cyfronet.s4e.TestJwtUtil.jwtBearerToken;

@AutoConfigureMockMvc
@BasicTest
@Slf4j
public class SavedViewControllerTest {
    private static Pattern UUID_PATTERN = Pattern.compile("[0-9A-Za-z]{8}-[0-9A-Za-z]{4}-[0-9A-Za-z]{4}-[0-9A-Za-z]{4}-[0-9A-Za-z]{12}");

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

    public interface SavedViewProjection {
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
                .thumbnail("some image")
                .configuration(Map.of("key-1", "val-1", "key-2", 25, "key-3", Map.of("key-3-1", true)))
                .build();

        assertThat(savedViewRepository.count(), is(equalTo(0L)));

        mockMvc.perform(post(API_PREFIX_V1 + "/savedViews")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(request))
                .with(jwtBearerToken(new AppUserDetails(appUser), objectMapper)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.uuid", matchesPattern(UUID_PATTERN)));

        val allSavedViews = savedViewRepository.findAllBy(SavedViewProjection.class);

        assertThat(allSavedViews, hasSize(1));
        val savedView = allSavedViews.get(0);
        assertThat(savedView.getCreatedAt(), is(lessThan(LocalDateTime.now())));
        assertThat(savedView.getCreatedAt(), is(greaterThan(LocalDateTime.now().minusSeconds(1))));
        assertThat(savedView.getOwner().getEmail(), is(equalTo("get@profile.com")));
    }

    @Test
    public void shouldntCreateSavedViewIfUserNotLoggedIn() throws Exception {
        CreateSavedViewRequest request = CreateSavedViewRequest.builder()
                .caption("view caption")
                .thumbnail("some image")
                .configuration(Map.of("key-1", "val-1", "key-2", 25, "key-3", Map.of("key-3-1", true)))
                .build();

        assertThat(savedViewRepository.count(), is(equalTo(0L)));

        mockMvc.perform(post(API_PREFIX_V1 + "/savedViews")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(request)))
                .andExpect(status().isForbidden());

        assertThat(savedViewRepository.count(), is(equalTo(0L)));
    }

    @Test
    public void shouldListSavedViews() throws Exception {
        val otherAppUser = appUserRepository.save(AppUser.builder()
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
                        .thumbnail("some image")
                        .configuration(Map.of("key-1", "val-1"))
                        .build(),
                SavedView.builder()
                        .owner(appUser)
                        .createdAt(LocalDateTime.of(2010, 1, 2, 0, 0))
                        .caption("caption-2")
                        .thumbnail("some image")
                        .configuration(Map.of("key-1", "val-1"))
                        .build(),
                SavedView.builder()
                        .owner(otherAppUser)
                        .createdAt(LocalDateTime.of(2010, 1, 2, 0, 0))
                        .caption("caption-3")
                        .thumbnail("some image")
                        .configuration(Map.of("key-1", "val-1"))
                        .build()));

        assertThat(savedViewRepository.count(), is(equalTo(3L)));

        mockMvc.perform(get(API_PREFIX_V1 + "/savedViews")
                .contentType(MediaType.APPLICATION_JSON)
                .with(jwtBearerToken(new AppUserDetails(appUser), objectMapper)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()", is(equalTo(2))))
                .andExpect(jsonPath("$.content..caption", contains("caption-2", "caption-1")));

        mockMvc.perform(get(API_PREFIX_V1 + "/savedViews")
                .contentType(MediaType.APPLICATION_JSON)
                .with(jwtBearerToken(new AppUserDetails(otherAppUser), objectMapper)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()", is(equalTo(1))))
                .andExpect(jsonPath("$.content..caption", contains("caption-3")));
    }

    @Test
    public void shouldDeleteSavedView() throws Exception {
        SavedView savedView = savedViewRepository.save(SavedView.builder()
                .owner(appUser)
                .createdAt(LocalDateTime.of(2010, 1, 1, 0, 0))
                .caption("caption-1")
                .thumbnail("some image")
                .configuration(Map.of("key-1", "val-1"))
                .build());

        assertThat(savedViewRepository.count(), is(equalTo(1L)));

        mockMvc.perform(delete(API_PREFIX_V1 + "/savedViews/{uuid}", savedView.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .with(jwtBearerToken(new AppUserDetails(appUser), objectMapper)))
                .andExpect(status().isOk());

        assertThat(savedViewRepository.count(), is(equalTo(0L)));
    }

    @Test
    public void shouldntDeleteSavedViewOfOtherUser() throws Exception {
        val otherAppUser = appUserRepository.save(AppUser.builder()
                .email("other@user.com")
                .name("Get")
                .surname("Profile")
                .password("{noop}password")
                .enabled(true)
                .build());

        SavedView savedView = savedViewRepository.save(SavedView.builder()
                .owner(otherAppUser)
                .createdAt(LocalDateTime.of(2010, 1, 1, 0, 0))
                .caption("caption-1")
                .thumbnail("some image")
                .configuration(Map.of("key-1", "val-1"))
                .build());

        assertThat(savedViewRepository.count(), is(equalTo(1L)));

        mockMvc.perform(delete(API_PREFIX_V1 + "/savedViews/{uuid}", savedView.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .with(jwtBearerToken(new AppUserDetails(appUser), objectMapper)))
                .andExpect(status().isForbidden());

        assertThat(savedViewRepository.count(), is(equalTo(1L)));
    }

    @Test
    public void shouldReturnForbiddenIfDeletingNonExistentSavedView() throws Exception {
        assertThat(savedViewRepository.count(), is(equalTo(0L)));

        mockMvc.perform(delete(API_PREFIX_V1 + "/savedViews/{uuid}", "6dea77af-1de9-4559-be06-0d50b7db1f35")
                .contentType(MediaType.APPLICATION_JSON)
                .with(jwtBearerToken(new AppUserDetails(appUser), objectMapper)))
                .andExpect(status().isForbidden());

        assertThat(savedViewRepository.count(), is(equalTo(0L)));
    }
}
