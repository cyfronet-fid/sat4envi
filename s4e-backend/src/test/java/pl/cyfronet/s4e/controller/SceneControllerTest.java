package pl.cyfronet.s4e.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import pl.cyfronet.s4e.BasicTest;
import pl.cyfronet.s4e.TestDbHelper;
import pl.cyfronet.s4e.bean.*;
import pl.cyfronet.s4e.data.repository.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.net.URL;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.time.temporal.ChronoUnit.HOURS;
import static java.time.temporal.ChronoUnit.MINUTES;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static pl.cyfronet.s4e.Constants.API_PREFIX_V1;
import static pl.cyfronet.s4e.SceneTestHelper.*;
import static pl.cyfronet.s4e.TestJwtUtil.jwtBearerToken;

@AutoConfigureMockMvc
@BasicTest
public class SceneControllerTest {
    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private SceneRepository sceneRepository;

    @Autowired
    private InstitutionRepository institutionRepository;

    @Autowired
    private UserRoleRepository userRoleRepository;

    @Autowired
    private LicenseGrantRepository licenseGrantRepository;

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private S3Presigner s3Presigner;

    @Autowired
    private Clock clock;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TestDbHelper testDbHelper;

    @BeforeEach
    public void beforeEach() {
        resetDb();
        reset(s3Presigner);
    }

    @AfterEach
    public void afterEach() {
        resetDb();
    }

    private void resetDb() {
        testDbHelper.clean();
    }

    @Test
    public void shouldReturnScenesWithArtifactsNames() throws Exception {
        val product = productRepository.save(productBuilder().build());
        sceneRepository.save(sceneBuilder(product, LocalDateTime.of(2019, 10, 11, 12, 13)).build());

        mockMvc.perform(get(API_PREFIX_V1 + "/products/" + product.getId() + "/scenes")
                .param("date", "2019-10-11"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(equalTo(1))))
                .andExpect(jsonPath("$[0].artifactNames", containsInAnyOrder("default_artifact", "other_artifact")));
    }

    @Test
    public void shouldReturnFilteredScenes() throws Exception {
        val product = productRepository.save(productBuilder().build());

        val scenes = Stream.of(
                LocalDateTime.of(2019, 10, 1, 0, 0),
                LocalDateTime.of(2019, 10, 1, 23, 59, 59),
                LocalDateTime.of(2019, 10, 2, 0, 0)
        )
                .map(toScene(product))
                .map(sceneRepository::save)
                .collect(Collectors.toList());

        mockMvc.perform(get(API_PREFIX_V1 + "/products/" + product.getId() + "/scenes")
                .param("date", "2019-10-01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(equalTo(2))))
                .andExpect(jsonPath("$..id", contains(
                        equalTo(scenes.get(0).getId().intValue()),
                        equalTo(scenes.get(1).getId().intValue()))));
    }

    @Test
    public void shouldReturnFilteredScenesInChosenTimeZone() throws Exception {
        val product = productRepository.save(productBuilder().build());

        val scenes = Stream.of(
                LocalDateTime.of(2019, 12, 1, 22, 59),
                LocalDateTime.of(2019, 12, 1, 23, 0),
                LocalDateTime.of(2019, 12, 2, 22, 59),
                LocalDateTime.of(2019, 12, 2, 23, 0)
        )
                .map(toScene(product))
                .map(sceneRepository::save)
                .collect(Collectors.toList());

        mockMvc.perform(get(API_PREFIX_V1 + "/products/" + product.getId() + "/scenes")
                .param("date", "2019-12-02")
                .param("timeZone", "Europe/Warsaw"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(equalTo(2))))
                .andExpect(jsonPath("$..id", contains(
                        equalTo(scenes.get(1).getId().intValue()),
                        equalTo(scenes.get(2).getId().intValue()))));
    }

    @Test
    public void shouldReturnZuluZonedTimestampByDefault() throws Exception {
        val product = productRepository.save(productBuilder().build());
        sceneRepository.save(sceneBuilder(product, LocalDateTime.of(2019, 10, 11, 12, 13)).build());

        mockMvc.perform(get(API_PREFIX_V1 + "/products/" + product.getId() + "/scenes")
                .param("date", "2019-10-11"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(equalTo(1))))
                .andExpect(jsonPath("$[0].timestamp").value(is(equalTo("2019-10-11T12:13:00Z"))));
    }

    @Test
    public void shouldReturnZonedTimestamp() throws Exception {
        val product = productRepository.save(productBuilder().build());

        sceneRepository.save(sceneBuilder(product, LocalDateTime.of(2019, 12, 1, 23, 0)).build());

        mockMvc.perform(get(API_PREFIX_V1 + "/products/" + product.getId() + "/scenes")
                .param("date", "2019-12-02")
                .param("timeZone", "Europe/Warsaw"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(equalTo(1))))
                .andExpect(jsonPath("$[0].timestamp").value(is(equalTo("2019-12-02T00:00:00+01:00"))));
    }

    @Test
    public void shouldReturnZonedTimestampsAroundDST() throws Exception {
        val product = productRepository.save(productBuilder().build());

        Stream.of(
                LocalDateTime.of(2019, 3, 31, 0, 59),
                LocalDateTime.of(2019, 3, 31, 1, 0),
                LocalDateTime.of(2019, 3, 31, 1, 1),
                LocalDateTime.of(2019, 10, 27, 0, 59),
                LocalDateTime.of(2019, 10, 27, 1, 0),
                LocalDateTime.of(2019, 10, 27, 1, 1)
        )
                .map(toScene(product))
                .forEach(sceneRepository::save);

        mockMvc.perform(get(API_PREFIX_V1 + "/products/" + product.getId() + "/scenes")
                .param("date", "2019-03-31")
                .param("timeZone", "Europe/Warsaw"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(equalTo(3))))
                .andExpect(jsonPath("$[0].timestamp").value(is(equalTo("2019-03-31T01:59:00+01:00"))))
                .andExpect(jsonPath("$[1].timestamp").value(is(equalTo("2019-03-31T03:00:00+02:00"))))
                .andExpect(jsonPath("$[2].timestamp").value(is(equalTo("2019-03-31T03:01:00+02:00"))));

        mockMvc.perform(get(API_PREFIX_V1 + "/products/" + product.getId() + "/scenes")
                .param("date", "2019-10-27")
                .param("timeZone", "Europe/Warsaw"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(equalTo(3))))
                .andExpect(jsonPath("$[0].timestamp").value(is(equalTo("2019-10-27T02:59:00+02:00"))))
                .andExpect(jsonPath("$[1].timestamp").value(is(equalTo("2019-10-27T02:00:00+01:00"))))
                .andExpect(jsonPath("$[2].timestamp").value(is(equalTo("2019-10-27T02:01:00+01:00"))));
    }

    @Test
    public void shouldReturn400IfZoneIncorrect() throws Exception {
        val product = productRepository.save(productBuilder().build());

        mockMvc.perform(get(API_PREFIX_V1 + "/products/" + product.getId() + "/scenes")
                .param("date", "2019-12-02")
                .param("timeZone", "incorrect"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void shouldReturnAvailabilityDates() throws Exception {
        val product = productRepository.save(productBuilder().build());

        /*
        We create data for the days marked with * and we will extract availability for October.
           Sep | Oct     | Nov
              *|**      *|*
         */
        Stream.of(
                LocalDateTime.of(2019, 9, 30, 23, 59, 59),
                LocalDateTime.of(2019, 10, 1, 0, 0),
                LocalDateTime.of(2019, 10, 2, 0, 0),
                LocalDateTime.of(2019, 10, 2, 1, 0),
                LocalDateTime.of(2019, 10, 31, 23, 59, 59),
                LocalDateTime.of(2019, 11, 1, 0, 0)
        )
                .map(toScene(product))
                .forEach(sceneRepository::save);

        mockMvc.perform(get(API_PREFIX_V1 + "/products/" + product.getId() + "/scenes/available")
                .param("yearMonth", "2019-10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(equalTo(3))))
                .andExpect(jsonPath("$", contains("2019-10-01", "2019-10-02", "2019-10-31")));
    }

    @Test
    public void shouldReturnAvailabilityDatesWithTimezone() throws Exception {
        val product = productRepository.save(productBuilder().build());

        /*
        We create data for the days marked with * and we will extract availability for October.
           Sep | Oct     | Nov
              *|**      *|*
         */
        Stream.of(
                LocalDateTime.of(2019, 9, 30, 21, 59, 59),
                LocalDateTime.of(2019, 9, 30, 22, 0),
                LocalDateTime.of(2019, 10, 2, 0, 0),
                LocalDateTime.of(2019, 10, 2, 1, 0),
                LocalDateTime.of(2019, 10, 31, 21, 59, 59)
        )
                .map(toScene(product))
                .forEach(sceneRepository::save);

        mockMvc.perform(get(API_PREFIX_V1 + "/products/" + product.getId() + "/scenes/available")
                .param("yearMonth", "2019-10")
                .param("timeZone", "Europe/Warsaw"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(equalTo(3))))
                .andExpect(jsonPath("$", contains("2019-10-01", "2019-10-02", "2019-10-31")));
    }

    @Nested
    class GetMostRecent {
        private final LocalDateTime BASE_TIME = getBaseTime();

        private Product product1;
        private Product product2;

        @BeforeEach
        public void beforeEach() {
            product1 = productRepository.save(productBuilder().build());
            product2 = productRepository.save(productBuilder().build());
            Stream.of(BASE_TIME.plusHours(1), BASE_TIME)
                    .map(toScene(product2))
                    .forEach(sceneRepository::save);
        }

        @Test
        public void shouldWork() throws Exception {
            val product1Scenes = Stream.of(BASE_TIME.minusSeconds(1), BASE_TIME, BASE_TIME.plusSeconds(1))
                    .map(toScene(product1))
                    .map(sceneRepository::save)
                    .collect(Collectors.toList());

            mockMvc.perform(get(API_PREFIX_V1 + "/products/" + product1.getId() + "/scenes/most-recent"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.sceneId", is(equalTo(product1Scenes.get(2).getId().intValue()))))
                    .andExpect(jsonPath("$.timestamp", is(equalTo("2020-01-01T00:00:01Z"))));

            mockMvc.perform(get(API_PREFIX_V1 + "/products/" + product1.getId() + "/scenes/most-recent")
                    .param("timeZone", "Europe/Warsaw"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.sceneId", is(equalTo(product1Scenes.get(2).getId().intValue()))))
                    .andExpect(jsonPath("$.timestamp", is(equalTo("2020-01-01T01:00:01+01:00"))));
        }

        @Test
        public void shouldReturn404IfProductDoesntExist() throws Exception {
            mockMvc.perform(get(API_PREFIX_V1 + "/products/" + (product2.getId() + 1) + "/scenes/most-recent"))
                    .andExpect(status().isNotFound());
        }

        @Test
        public void shouldReturnEmptyIfThereAreNoScenes() throws Exception {
            Stream.of(BASE_TIME.plusHours(1), BASE_TIME)
                    .map(toScene(product2))
                    .forEach(sceneRepository::save);

            mockMvc.perform(get(API_PREFIX_V1 + "/products/" + product1.getId() + "/scenes/most-recent"))
                    .andExpect(status().isOk())
                    // Expect the one with lower id to be returned.
                    .andExpect(jsonPath("$.sceneId", is(nullValue())))
                    .andExpect(jsonPath("$.timestamp", is(nullValue())));
        }

        @Nested
        class EumetsatLicense {
            private AppUser appUser;

            @BeforeEach
            public void beforeEach() {
                appUser = appUserRepository.save(AppUser.builder()
                        .email("get@profile.com")
                        .name("Get")
                        .surname("Profile")
                        .password("{noop}password")
                        .enabled(true)
                        .eumetsatLicense(true)
                        .build());

                product1.setAccessType(Product.AccessType.EUMETSAT);
                productRepository.save(product1);
            }

            @Test
            public void shouldExcludeLicensedScenes() throws Exception {
                val product1Scenes = Stream.of(BASE_TIME.minus(2, MINUTES), BASE_TIME.minus(1, HOURS))
                        .map(toScene(product1))
                        .map(sceneRepository::save)
                        .collect(Collectors.toList());

                mockMvc.perform(get(API_PREFIX_V1 + "/products/" + product1.getId() + "/scenes/most-recent"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.sceneId", is(equalTo(product1Scenes.get(1).getId().intValue()))));
            }

            @Test
            public void shouldIncludeLicensedScenesForLicensedUser() throws Exception {
                val product1Scenes = Stream.of(BASE_TIME.minus(2, MINUTES), BASE_TIME.minus(1, HOURS))
                        .map(toScene(product1))
                        .map(sceneRepository::save)
                        .collect(Collectors.toList());

                mockMvc.perform(get(API_PREFIX_V1 + "/products/" + product1.getId() + "/scenes/most-recent")
                        .with(jwtBearerToken(appUser, objectMapper)))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.sceneId", is(equalTo(product1Scenes.get(0).getId().intValue()))));
            }
        }

        @Nested
        class PrivateLicense {
            private AppUser appUser;

            @BeforeEach
            public void beforeEach() {
                appUser = appUserRepository.save(AppUser.builder()
                        .email("get@profile.com")
                        .name("Get")
                        .surname("Profile")
                        .password("{noop}password")
                        .enabled(true)
                        .build());

                product1.setAccessType(Product.AccessType.PRIVATE);
                productRepository.save(product1);

                val institution = institutionRepository.save(Institution.builder()
                        .name("test")
                        .slug("test")
                        .build());

                userRoleRepository.save(UserRole.builder().
                        role(AppRole.INST_MEMBER)
                        .user(appUser)
                        .institution(institution)
                        .build());

                licenseGrantRepository.save(LicenseGrant.builder()
                        .institution(institution)
                        .product(product1)
                        .build());
            }

            @Test
            public void shouldForbidNotLicensedUser() throws Exception {
                mockMvc.perform(get(API_PREFIX_V1 + "/products/" + product1.getId() + "/scenes/most-recent"))
                        .andExpect(status().isUnauthorized());
            }

            @Test
            public void shouldAllowLicensedUser() throws Exception {
                mockMvc.perform(get(API_PREFIX_V1 + "/products/" + product1.getId() + "/scenes/most-recent")
                        .with(jwtBearerToken(appUser, objectMapper)))
                        .andExpect(status().isOk());
            }
        }
    }

    @Nested
    class Download {
        private AppUser appUser;

        @BeforeEach
        public void beforeEach() {
            appUser = appUserRepository.save(AppUser.builder()
                    .email("get@profile.com")
                    .name("Get")
                    .surname("Profile")
                    .password("{noop}password")
                    .enabled(true)
                    .build());
        }

        @Test
        public void shouldRedirectToDownloadLink() throws Exception {
            val product = productRepository.save(productBuilder().build());

            Scene scene = sceneRepository.save(sceneBuilder(product).build());

            String redirectUrl = "https://domain.pl/test?sth=value";

            PresignedGetObjectRequest pgor = mock(PresignedGetObjectRequest.class);
            when(pgor.isBrowserExecutable()).thenReturn(true);
            when(pgor.url()).thenReturn(new URL(redirectUrl));
            when(s3Presigner.presignGetObject(any(GetObjectPresignRequest.class))).thenReturn(pgor);

            mockMvc.perform(get(API_PREFIX_V1 + "/scenes/{id}/download/{artifactName}", scene.getId(), "default_artifact")
                    .with(jwtBearerToken(appUser, objectMapper)))
                    .andExpect(status().isSeeOther())
                    .andExpect(redirectedUrl(redirectUrl));
        }

        @Test
        public void shouldReturn401IfUnauthenticated() throws Exception {
            mockMvc.perform(get(API_PREFIX_V1 + "/scenes/{id}/download/{artifactName}", -1L, "default_artifact"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        public void shouldReturn404IfSceneNotFound() throws Exception {
            mockMvc.perform(get(API_PREFIX_V1 + "/scenes/{id}/download/{artifactName}", -1L, "default_artifact")
                    .with(jwtBearerToken(appUser, objectMapper)))
                    .andExpect(status().isNotFound());
        }

        @Test
        public void shouldReturn404IfArtifactNotFound() throws Exception {
            mockMvc.perform(get(API_PREFIX_V1 + "/scenes/{id}/download/{artifactName}", -1L, "non_existent")
                    .with(jwtBearerToken(appUser, objectMapper)))
                    .andExpect(status().isNotFound());
        }
    }

    private LocalDateTime getBaseTime() {
        ZonedDateTime zonedDateTime = clock.instant().atZone(ZoneId.of("UTC"));
        return LocalDateTime.from(zonedDateTime);
    }
}

