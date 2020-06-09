package pl.cyfronet.s4e.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import pl.cyfronet.s4e.BasicTest;
import pl.cyfronet.s4e.SceneTestHelper;
import pl.cyfronet.s4e.TestDbHelper;
import pl.cyfronet.s4e.bean.Product;
import pl.cyfronet.s4e.bean.Scene;
import pl.cyfronet.s4e.data.repository.ProductRepository;
import pl.cyfronet.s4e.data.repository.SceneRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static pl.cyfronet.s4e.Constants.API_PREFIX_V1;
import static pl.cyfronet.s4e.SceneTestHelper.productBuilder;

@AutoConfigureMockMvc
@BasicTest
@Slf4j
public class SearchControllerTest {
    @Autowired
    private SceneRepository sceneRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private TestDbHelper testDbHelper;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    public void setUp() throws Exception {
        testDbHelper.clean();
        //add product
        Product product = productRepository.save(productBuilder().build());
        //addscenewithmetadata
        List<Scene> scenes = new ArrayList<>();
        for (long j = 0; j < 30; j++) {
            scenes.add(buildScene(product, j));
        }
        sceneRepository.saveAll(scenes);

    }

    @AfterEach
    public void tearDown() {
        testDbHelper.clean();
    }

    private Scene buildScene(Product product, long number) throws Exception {
        JsonNode jsonNode = objectMapper.readTree(SceneTestHelper.getMetaDataWithNumber(number));
        return SceneTestHelper.sceneWithMetadataBuilder(product, jsonNode)
                .build();
    }

    @Test
    public void shouldGetSceneBySearchEndpoint() throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("timeZone", "UTC");

        // default limit is 20
        int limit = 20;
        mockMvc.perform(get(API_PREFIX_V1 + "/search")
                .param("timeZone", "UTC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(equalTo(limit))));
    }

    @Test
    public void shouldGetSceneBySensingTime() throws Exception {
        mockMvc.perform(get(API_PREFIX_V1 + "/search")
                .param("timeZone", "UTC")
                .param("sensingFrom", "2019-11-07T00:00:00.000000-07:00")
                .param("sensingTo", "2019-11-12T00:00:00.000000+00:00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(equalTo(6))));
    }

    @Test
    public void shouldntGetSceneBySensingTimeWrongFormat() throws Exception {
        mockMvc.perform(get(API_PREFIX_V1 + "/search")
                .param("timeZone", "UTC")
                .param("sensingFrom", "2019-11-08")
                .param("sensingTo", "2019-11-12"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.__general__", is("Cannot parse date: 2019-11-08")));
    }

    @Test
    public void shouldGetSceneByIngestionTime() throws Exception {
        mockMvc.perform(get(API_PREFIX_V1 + "/search?timeZone=UTC&collection=S1B_24AU")
                .param("timeZone", "UTC")
                .param("ingestionFrom", "2019-11-08T00:00:00.000000-07:00")
                .param("ingestionTo", "2019-11-12T00:00:00.000000+00:00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(equalTo(3))));
    }

    @Test
    public void shouldntGetSceneByIngestionTimeWrongFormat() throws Exception {
        mockMvc.perform(get(API_PREFIX_V1 + "/search")
                .param("timeZone", "UTC")
                .param("ingestionFrom", "2019-11-08")
                .param("ingestionTo", "2019-11-12"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.__general__", is("Cannot parse date: 2019-11-08")));
    }

    @Test
    public void shouldGetSceneBySatellitePlatform() throws Exception {
        mockMvc.perform(get(API_PREFIX_V1 + "/search")
                .param("satellitePlatform", "Sentinel-1A")
                .param("limit", "30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(equalTo(30))))
                .andReturn();
    }

    @Test
    public void shouldGetSceneByProductType() throws Exception {
        mockMvc.perform(get(API_PREFIX_V1 + "/search")
                .param("productType", "GRDH")
                .param("limit", "30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(equalTo(30))))
                .andReturn();
    }

    @Test
    public void shouldGetSceneByPolarisation() throws Exception {
        mockMvc.perform(get(API_PREFIX_V1 + "/search")
                .param("polarisation",  "Dual VV/VH")
                .param("limit", "30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(equalTo(30))))
                .andReturn();
    }

    @Test
    public void shouldGetSceneBySensorMode() throws Exception {
        mockMvc.perform(get(API_PREFIX_V1 + "/search")
                .param("sensorMode", "IW")
                .param("limit", "30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(equalTo(30))))
                .andReturn();
    }

    @Test
    public void shouldGetSceneByCollection() throws Exception {
        mockMvc.perform(get(API_PREFIX_V1 + "/search")
                .param("collection", "S1B_24AU")
                .param("limit", "30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(equalTo(30))));
    }

    @Test
    public void shouldGetSceneByByCloudCover() throws Exception {
        mockMvc.perform(get(API_PREFIX_V1 + "/search")
                .param("cloudCover", "0.4f")
                .param("limit", "30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(equalTo(25))));
    }

    @Test
    public void shouldGetSceneByByTimeliness() throws Exception {
        mockMvc.perform(get(API_PREFIX_V1 + "/search")
                .param("timeliness", "Near Real Time")
                .param("limit", "30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(equalTo(30))));
    }

    @Test
    public void shouldGetSceneByProcessingLevel() throws Exception {
        mockMvc.perform(get(API_PREFIX_V1 + "/search")
                .param("processingLevel", "2LC")
                .param("limit", "30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(equalTo(3))));
    }

    @Test
    public void shouldGetSceneByProductLevel() throws Exception {
        mockMvc.perform(get(API_PREFIX_V1 + "/search")
                .param("productLevel", "L2")
                .param("limit", "30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(equalTo(3))));
    }

    @Test
    public void shouldGetSceneWithSortBySensingTime() throws Exception {
        mockMvc.perform(get(API_PREFIX_V1 + "/search")
                .param("sortBy", "sensingTime")
                .param("limit", "30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(equalTo(30))));
    }

    @Test
    public void shouldGetSceneWithSortByIngestionTime() throws Exception {
        mockMvc.perform(get(API_PREFIX_V1 + "/search")
                .param("sortBy", "ingestionTime")
                .param("limit", "30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(equalTo(30))));
    }
}
