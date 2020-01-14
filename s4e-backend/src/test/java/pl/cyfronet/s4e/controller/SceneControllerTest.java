package pl.cyfronet.s4e.controller;

import lombok.val;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import pl.cyfronet.s4e.BasicTest;
import pl.cyfronet.s4e.bean.Scene;
import pl.cyfronet.s4e.bean.Product;
import pl.cyfronet.s4e.data.repository.SceneRepository;
import pl.cyfronet.s4e.data.repository.ProductRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static pl.cyfronet.s4e.Constants.API_PREFIX_V1;
import static org.hamcrest.Matchers.*;

@AutoConfigureMockMvc
@BasicTest
public class SceneControllerTest {
    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private SceneRepository sceneRepository;

    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    public void beforeEach() {
        reset();
    }

    @AfterEach
    public void afterEach() {
        reset();
    }

    private void reset() {
        sceneRepository.deleteAll();
        productRepository.deleteAll();
    }

    @Test
    public void shouldReturnFilteredScenes() throws Exception {
        val product = productRepository.save(Product.builder()
                .name("108m")
                .description("sth")
                .build());

        val scenes = List.of(
                Scene.builder()
                        .product(product)
                        .layerName("testLayerName1")
                        .timestamp(LocalDateTime.of(2019, 10, 1, 0, 0))
                        .s3Path("some/path")
                        .build(),
                Scene.builder()
                        .product(product)
                        .layerName("testLayerName2")
                        .timestamp(LocalDateTime.of(2019, 10, 1, 23, 59, 59))
                        .s3Path("some/path")
                        .build(),
                Scene.builder()
                        .product(product)
                        .layerName("testLayerName3")
                        .timestamp(LocalDateTime.of(2019, 10, 2, 0, 0))
                        .s3Path("some/path")
                        .build()
        );
        sceneRepository.saveAll(scenes);

        mockMvc.perform(get(API_PREFIX_V1 + "/products/" + product.getId() + "/scenes")
                .contentType(MediaType.APPLICATION_JSON)
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
                .description("sth")
                .build());

        val scenes = List.of(
                Scene.builder()
                        .product(product)
                        .layerName("testLayerName1")
                        .timestamp(LocalDateTime.of(2019, 12, 1, 22, 59))
                        .s3Path("some/path")
                        .build(),
                Scene.builder()
                        .product(product)
                        .layerName("testLayerName2")
                        .timestamp(LocalDateTime.of(2019, 12, 1, 23, 0))
                        .s3Path("some/path")
                        .build(),
                Scene.builder()
                        .product(product)
                        .layerName("testLayerName3")
                        .timestamp(LocalDateTime.of(2019, 12, 2, 22, 59))
                        .s3Path("some/path")
                        .build(),
                Scene.builder()
                        .product(product)
                        .layerName("testLayerName4")
                        .timestamp(LocalDateTime.of(2019, 12, 2, 23, 0))
                        .s3Path("some/path")
                        .build()
        );
        sceneRepository.saveAll(scenes);

        mockMvc.perform(get(API_PREFIX_V1 + "/products/" + product.getId() + "/scenes")
                .contentType(MediaType.APPLICATION_JSON)
                .param("date", "2019-12-02")
                .param("tz", "Europe/Warsaw"))
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
                .description("sth")
                .build());
        sceneRepository.save(Scene.builder()
                .product(product)
                .layerName("testLayerName")
                .timestamp(LocalDateTime.of(2019, 10, 11, 12, 13))
                .s3Path("some/path")
                .build());

        mockMvc.perform(get(API_PREFIX_V1 + "/products/" + product.getId() + "/scenes")
                .contentType(MediaType.APPLICATION_JSON)
                .param("date", "2019-10-11"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(equalTo(1))))
                .andExpect(jsonPath("$[0].timestamp").value(is(equalTo("2019-10-11T12:13:00Z"))));
    }

    @Test
    public void shouldReturnZonedTimestamp() throws Exception {
        val product = productRepository.save(Product.builder()
                .name("108m")
                .description("sth")
                .build());

        val scenes = List.of(
                Scene.builder()
                        .product(product)
                        .layerName("testLayerName1")
                        .timestamp(LocalDateTime.of(2019, 12, 1, 23, 0))
                        .s3Path("some/path")
                        .build()
        );
        sceneRepository.saveAll(scenes);

        mockMvc.perform(get(API_PREFIX_V1 + "/products/" + product.getId() + "/scenes")
                .contentType(MediaType.APPLICATION_JSON)
                .param("date", "2019-12-02")
                .param("tz", "Europe/Warsaw"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(equalTo(1))))
                .andExpect(jsonPath("$[0].timestamp").value(is(equalTo("2019-12-02T00:00:00+01:00"))));
    }

    @Test
    public void shouldReturnZonedTimestampsAroundDST() throws Exception {
        val product = productRepository.save(Product.builder()
                .name("108m")
                .description("sth")
                .build());

        val scenes = List.of(
                Scene.builder()
                        .product(product)
                        .layerName("testLayerName1")
                        .timestamp(LocalDateTime.of(2019, 3, 31, 0, 59))
                        .s3Path("some/path")
                        .build(),
                Scene.builder()
                        .product(product)
                        .layerName("testLayerName2")
                        .timestamp(LocalDateTime.of(2019, 3, 31, 1, 0))
                        .s3Path("some/path")
                        .build(),
                Scene.builder()
                        .product(product)
                        .layerName("testLayerName3")
                        .timestamp(LocalDateTime.of(2019, 3, 31, 1, 1))
                        .s3Path("some/path")
                        .build(),
                Scene.builder()
                        .product(product)
                        .layerName("testLayerName4")
                        .timestamp(LocalDateTime.of(2019, 10, 27, 0, 59))
                        .s3Path("some/path")
                        .build(),
                Scene.builder()
                        .product(product)
                        .layerName("testLayerName5")
                        .timestamp(LocalDateTime.of(2019, 10, 27, 1, 0))
                        .s3Path("some/path")
                        .build(),
                Scene.builder()
                        .product(product)
                        .layerName("testLayerName6")
                        .timestamp(LocalDateTime.of(2019, 10, 27, 1, 1))
                        .s3Path("some/path")
                        .build()
        );
        sceneRepository.saveAll(scenes);

        mockMvc.perform(get(API_PREFIX_V1 + "/products/" + product.getId() + "/scenes")
                .contentType(MediaType.APPLICATION_JSON)
                .param("date", "2019-03-31")
                .param("tz", "Europe/Warsaw"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(equalTo(3))))
                .andExpect(jsonPath("$[0].timestamp").value(is(equalTo("2019-03-31T01:59:00+01:00"))))
                .andExpect(jsonPath("$[1].timestamp").value(is(equalTo("2019-03-31T03:00:00+02:00"))))
                .andExpect(jsonPath("$[2].timestamp").value(is(equalTo("2019-03-31T03:01:00+02:00"))));

        mockMvc.perform(get(API_PREFIX_V1 + "/products/" + product.getId() + "/scenes")
                .contentType(MediaType.APPLICATION_JSON)
                .param("date", "2019-10-27")
                .param("tz", "Europe/Warsaw"))
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
                .description("sth")
                .build());

        mockMvc.perform(get(API_PREFIX_V1 + "/products/" + product.getId() + "/scenes")
                .contentType(MediaType.APPLICATION_JSON)
                .param("date", "2019-12-02")
                .param("tz", "incorrect"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void shouldReturnAvailabilityDates() throws Exception {
        val product = productRepository.save(Product.builder()
                .name("108m")
                .description("sth")
                .build());

        /*
        We create data for the days marked with * and we will extract availability for October.
           Sep | Oct     | Nov
              *|**      *|*
         */
        val scenes = List.of(
                Scene.builder()
                        .product(product)
                        .layerName("testLayerName0")
                        .timestamp(LocalDateTime.of(2019, 9, 30, 23, 59, 59))
                        .s3Path("some/path")
                        .build(),
                Scene.builder()
                        .product(product)
                        .layerName("testLayerName1")
                        .timestamp(LocalDateTime.of(2019, 10, 1, 0, 0))
                        .s3Path("some/path")
                        .build(),
                Scene.builder()
                        .product(product)
                        .layerName("testLayerName2")
                        .timestamp(LocalDateTime.of(2019, 10, 2, 0, 0))
                        .s3Path("some/path")
                        .build(),
                Scene.builder()
                        .product(product)
                        .layerName("testLayerName3")
                        .timestamp(LocalDateTime.of(2019, 10, 2, 1, 0))
                        .s3Path("some/path")
                        .build(),
                Scene.builder()
                        .product(product)
                        .layerName("testLayerName4")
                        .timestamp(LocalDateTime.of(2019, 10, 31, 23, 59, 59))
                        .s3Path("some/path")
                        .build(),
                Scene.builder()
                        .product(product)
                        .layerName("testLayerName5")
                        .timestamp(LocalDateTime.of(2019, 11, 1, 0, 0))
                        .s3Path("some/path")
                        .build()
        );
        sceneRepository.saveAll(scenes);

        mockMvc.perform(get(API_PREFIX_V1 + "/products/" + product.getId() + "/scenes/available")
                .contentType(MediaType.APPLICATION_JSON)
                .param("yearMonth", "2019-10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(equalTo(3))))
                .andExpect(jsonPath("$", contains("2019-10-01", "2019-10-02", "2019-10-31")));
    }

    @Test
    public void shouldReturnAvailabilityDatesWithTimezone() throws Exception {
        val product = productRepository.save(Product.builder()
                .name("108m")
                .description("sth")
                .build());

        /*
        We create data for the days marked with * and we will extract availability for October.
           Sep | Oct     | Nov
              *|**      *|*
         */
        val scenes = List.of(
                Scene.builder()
                        .product(product)
                        .layerName("testLayerName0")
                        .timestamp(LocalDateTime.of(2019, 9, 30, 21, 59, 59))
                        .s3Path("some/path")
                        .build(),
                Scene.builder()
                        .product(product)
                        .layerName("testLayerName1")
                        .timestamp(LocalDateTime.of(2019, 9, 30, 22, 0))
                        .s3Path("some/path")
                        .build(),
                Scene.builder()
                        .product(product)
                        .layerName("testLayerName2")
                        .timestamp(LocalDateTime.of(2019, 10, 2, 0, 0))
                        .s3Path("some/path")
                        .build(),
                Scene.builder()
                        .product(product)
                        .layerName("testLayerName3")
                        .timestamp(LocalDateTime.of(2019, 10, 2, 1, 0))
                        .s3Path("some/path")
                        .build(),
                Scene.builder()
                        .product(product)
                        .layerName("testLayerName4")
                        .timestamp(LocalDateTime.of(2019, 10, 31, 21, 59, 59))
                        .s3Path("some/path")
                        .build()
        );
        sceneRepository.saveAll(scenes);

        mockMvc.perform(get(API_PREFIX_V1 + "/products/" + product.getId() + "/scenes/available")
                .contentType(MediaType.APPLICATION_JSON)
                .param("yearMonth", "2019-10")
                .param("tz", "Europe/Warsaw"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(equalTo(3))))
                .andExpect(jsonPath("$", contains("2019-10-01", "2019-10-02", "2019-10-31")));
    }
}

