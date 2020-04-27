package pl.cyfronet.s4e.controller;

import lombok.val;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import pl.cyfronet.s4e.BasicTest;
import pl.cyfronet.s4e.TestDbHelper;
import pl.cyfronet.s4e.TestGeometryHelper;
import pl.cyfronet.s4e.bean.Product;
import pl.cyfronet.s4e.bean.Scene;
import pl.cyfronet.s4e.data.repository.ProductRepository;
import pl.cyfronet.s4e.data.repository.SceneRepository;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.net.URL;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static pl.cyfronet.s4e.Constants.API_PREFIX_V1;

@AutoConfigureMockMvc
@BasicTest
public class SceneControllerTest {
    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private SceneRepository sceneRepository;

    @Autowired
    private S3Presigner s3Presigner;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TestDbHelper testDbHelper;

    @Autowired
    private TestGeometryHelper geom;

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
    public void shouldReturnFilteredScenes() throws Exception {
        val product = productRepository.save(Product.builder()
                .name("108m")
                .displayName("108m")
                .description("sth")
                .layerName("108m")
                .build());

        val defaultBuilder = Scene.builder()
                .product(product)
                .s3Path("some/path")
                .granulePath("mailto://bucket/some/path")
                .footprint(geom.any());

        val scenes = List.of(
                defaultBuilder
                        .timestamp(LocalDateTime.of(2019, 10, 1, 0, 0))
                        .build(),
                defaultBuilder
                        .timestamp(LocalDateTime.of(2019, 10, 1, 23, 59, 59))
                        .build(),
                defaultBuilder
                        .timestamp(LocalDateTime.of(2019, 10, 2, 0, 0))
                        .build()
        );
        sceneRepository.saveAll(scenes);

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
        val product = productRepository.save(Product.builder()
                .name("108m")
                .displayName("108m")
                .description("sth")
                .layerName("108m")
                .build());

        val defaultBuilder = Scene.builder()
                .product(product)
                .s3Path("some/path")
                .granulePath("mailto://bucket/some/path")
                .footprint(geom.any());

        val scenes = List.of(
                defaultBuilder
                        .timestamp(LocalDateTime.of(2019, 12, 1, 22, 59))
                        .build(),
                defaultBuilder
                        .timestamp(LocalDateTime.of(2019, 12, 1, 23, 0))
                        .build(),
                defaultBuilder
                        .timestamp(LocalDateTime.of(2019, 12, 2, 22, 59))
                        .build(),
                defaultBuilder
                        .timestamp(LocalDateTime.of(2019, 12, 2, 23, 0))
                        .build()
        );
        sceneRepository.saveAll(scenes);

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
        val product = productRepository.save(Product.builder()
                .name("108m")
                .displayName("108m")
                .description("sth")
                .layerName("108m")
                .build());
        sceneRepository.save(Scene.builder()
                .product(product)
                .timestamp(LocalDateTime.of(2019, 10, 11, 12, 13))
                .s3Path("some/path")
                .granulePath("mailto://bucket/some/path")
                .footprint(geom.any())
                .build());

        mockMvc.perform(get(API_PREFIX_V1 + "/products/" + product.getId() + "/scenes")
                .param("date", "2019-10-11"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(equalTo(1))))
                .andExpect(jsonPath("$[0].timestamp").value(is(equalTo("2019-10-11T12:13:00Z"))));
    }

    @Test
    public void shouldReturnZonedTimestamp() throws Exception {
        val product = productRepository.save(Product.builder()
                .name("108m")
                .displayName("108m")
                .description("sth")
                .layerName("108m")
                .build());

        val scenes = List.of(
                Scene.builder()
                        .product(product)
                        .timestamp(LocalDateTime.of(2019, 12, 1, 23, 0))
                        .s3Path("some/path")
                        .granulePath("mailto://bucket/some/path")
                        .footprint(geom.any())
                        .build()
        );
        sceneRepository.saveAll(scenes);

        mockMvc.perform(get(API_PREFIX_V1 + "/products/" + product.getId() + "/scenes")
                .param("date", "2019-12-02")
                .param("timeZone", "Europe/Warsaw"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(equalTo(1))))
                .andExpect(jsonPath("$[0].timestamp").value(is(equalTo("2019-12-02T00:00:00+01:00"))));
    }

    @Test
    public void shouldReturnZonedTimestampsAroundDST() throws Exception {
        val product = productRepository.save(Product.builder()
                .name("108m")
                .displayName("108m")
                .description("sth")
                .layerName("108m")
                .build());

        val defaultBuilder = Scene.builder()
                .product(product)
                .s3Path("some/path")
                .granulePath("mailto://bucket/some/path")
                .footprint(geom.any());

        val scenes = List.of(
                defaultBuilder
                        .timestamp(LocalDateTime.of(2019, 3, 31, 0, 59))
                        .build(),
                defaultBuilder
                        .timestamp(LocalDateTime.of(2019, 3, 31, 1, 0))
                        .build(),
                defaultBuilder
                        .timestamp(LocalDateTime.of(2019, 3, 31, 1, 1))
                        .build(),
                defaultBuilder
                        .timestamp(LocalDateTime.of(2019, 10, 27, 0, 59))
                        .build(),
                defaultBuilder
                        .timestamp(LocalDateTime.of(2019, 10, 27, 1, 0))
                        .build(),
                defaultBuilder
                        .timestamp(LocalDateTime.of(2019, 10, 27, 1, 1))
                        .build()
        );
        sceneRepository.saveAll(scenes);

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
        val product = productRepository.save(Product.builder()
                .name("108m")
                .displayName("108m")
                .description("sth")
                .layerName("108m")
                .build());

        mockMvc.perform(get(API_PREFIX_V1 + "/products/" + product.getId() + "/scenes")
                .param("date", "2019-12-02")
                .param("timeZone", "incorrect"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void shouldReturnAvailabilityDates() throws Exception {
        val product = productRepository.save(Product.builder()
                .name("108m")
                .displayName("108m")
                .description("sth")
                .layerName("108m")
                .build());

        val defaultBuilder = Scene.builder()
                .product(product)
                .s3Path("some/path")
                .granulePath("mailto://bucket/some/path")
                .footprint(geom.any());

        /*
        We create data for the days marked with * and we will extract availability for October.
           Sep | Oct     | Nov
              *|**      *|*
         */
        val scenes = List.of(
                defaultBuilder
                        .timestamp(LocalDateTime.of(2019, 9, 30, 23, 59, 59))
                        .build(),
                defaultBuilder
                        .timestamp(LocalDateTime.of(2019, 10, 1, 0, 0))
                        .build(),
                defaultBuilder
                        .timestamp(LocalDateTime.of(2019, 10, 2, 0, 0))
                        .build(),
                defaultBuilder
                        .timestamp(LocalDateTime.of(2019, 10, 2, 1, 0))
                        .build(),
                defaultBuilder
                        .timestamp(LocalDateTime.of(2019, 10, 31, 23, 59, 59))
                        .build(),
                defaultBuilder
                        .timestamp(LocalDateTime.of(2019, 11, 1, 0, 0))
                        .build()
        );
        sceneRepository.saveAll(scenes);

        mockMvc.perform(get(API_PREFIX_V1 + "/products/" + product.getId() + "/scenes/available")
                .param("yearMonth", "2019-10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(equalTo(3))))
                .andExpect(jsonPath("$", contains("2019-10-01", "2019-10-02", "2019-10-31")));
    }

    @Test
    public void shouldReturnAvailabilityDatesWithTimezone() throws Exception {
        val product = productRepository.save(Product.builder()
                .name("108m")
                .displayName("108m")
                .description("sth")
                .layerName("108m")
                .build());

        val defaultBuilder = Scene.builder()
                .product(product)
                .s3Path("some/path")
                .granulePath("mailto://bucket/some/path")
                .footprint(geom.any());

        /*
        We create data for the days marked with * and we will extract availability for October.
           Sep | Oct     | Nov
              *|**      *|*
         */
        val scenes = List.of(
                defaultBuilder
                        .timestamp(LocalDateTime.of(2019, 9, 30, 21, 59, 59))
                        .build(),
                defaultBuilder
                        .timestamp(LocalDateTime.of(2019, 9, 30, 22, 0))
                        .build(),
                defaultBuilder
                        .timestamp(LocalDateTime.of(2019, 10, 2, 0, 0))
                        .build(),
                defaultBuilder
                        .timestamp(LocalDateTime.of(2019, 10, 2, 1, 0))
                        .build(),
                defaultBuilder
                        .timestamp(LocalDateTime.of(2019, 10, 31, 21, 59, 59))
                        .build()
        );
        sceneRepository.saveAll(scenes);

        mockMvc.perform(get(API_PREFIX_V1 + "/products/" + product.getId() + "/scenes/available")
                .param("yearMonth", "2019-10")
                .param("timeZone", "Europe/Warsaw"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(equalTo(3))))
                .andExpect(jsonPath("$", contains("2019-10-01", "2019-10-02", "2019-10-31")));
    }

    @Test
    public void shouldRedirectToDownloadLink() throws Exception {
        val product = productRepository.save(Product.builder()
                .name("108m")
                .displayName("108m")
                .description("sth")
                .layerName("108m")
                .build());

        Scene scene = sceneRepository.save(Scene.builder()
                .product(product)
                .timestamp(LocalDateTime.of(2019, 10, 1, 0, 0))
                .s3Path("some/path")
                .granulePath("mailto://bucket/some/path")
                .footprint(geom.any())
                .build());

        String redirectUrl = "https://domain.pl/test?sth=value";

        PresignedGetObjectRequest pgor = mock(PresignedGetObjectRequest.class);
        when(pgor.isBrowserExecutable()).thenReturn(true);
        when(pgor.url()).thenReturn(new URL(redirectUrl));
        when(s3Presigner.presignGetObject(any(GetObjectPresignRequest.class))).thenReturn(pgor);

        mockMvc.perform(get(API_PREFIX_V1 + "/scenes/{id}/download", scene.getId()))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl(redirectUrl));
    }

    @Test
    public void shouldReturn404IfSceneNotFound() throws Exception {
        mockMvc.perform(get(API_PREFIX_V1 + "/scenes/{id}/download", 42L))
                .andExpect(status().isNotFound());
    }
}

