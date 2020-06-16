package pl.cyfronet.s4e.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import pl.cyfronet.s4e.BasicTest;
import pl.cyfronet.s4e.SceneTestHelper;
import pl.cyfronet.s4e.TestDbHelper;
import pl.cyfronet.s4e.bean.Product;
import pl.cyfronet.s4e.bean.Scene;
import pl.cyfronet.s4e.data.repository.ProductRepository;
import pl.cyfronet.s4e.data.repository.SceneRepository;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static pl.cyfronet.s4e.SceneTestHelper.productBuilder;

@BasicTest
@Slf4j
public class SearchServiceTest {
    @Autowired
    private SceneRepository sceneRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private TestDbHelper testDbHelper;

    @Autowired
    private SearchService searchService;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setUp() throws Exception {
        testDbHelper.clean();
        //add product
        Product product = productRepository.save(productBuilder().build());
        //addscenewithmetadata
        List<Scene> scenes = new ArrayList<>();
        for (long j = 0; j < 10; j++) {
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
    public void testQueryBySensingTime() throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("sensingFrom", "2019-11-05T00:00:00.000000+00:00");
        params.put("sensingTo", "2019-11-10T00:00:00.000000+00:00");
        List<Scene> scenes = searchService.getScenesBy(params);
        assertThat(scenes, hasSize(5));
    }

    @Test
    public void testQueryByIngestionTime() throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("ingestionFrom", "2019-11-09T00:00:00.000000+00:00");
        params.put("ingestionTo", "2019-11-12T00:00:00.000000+00:00");
        List<Scene> scenes = searchService.getScenesBy(params);
        assertThat(scenes, hasSize(1));
    }

    @Test
    public void testQueryBySatellitePlatform() throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("satellitePlatform", "Sentinel-1A");
        List<Scene> scenes = searchService.getScenesBy(params);
        assertThat(scenes, hasSize(10));
    }

    @Test
    public void testQueryByProductType() throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("productType", "GRDH");
        List<Scene> scenes = searchService.getScenesBy(params);
        assertThat(scenes, hasSize(10));
    }

    @Test
    public void testQueryByPolarisation() throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("polarisation", "VV VH");
        List<Scene> scenes = searchService.getScenesBy(params);
        assertThat(scenes, hasSize(10));
    }

    @Test
    public void testQueryBySensorMode() throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("sensorMode", "IW");
        List<Scene> scenes = searchService.getScenesBy(params);
        assertThat(scenes, hasSize(10));
    }

    @Test
    public void testQueryByCollection() throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("collection", "S1B_24AU");
        List<Scene> scenes = searchService.getScenesBy(params);
        assertThat(scenes, hasSize(10));
    }

    @Test
    public void testQueryByCloudCover() throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("cloudCover", 0.4f);
        List<Scene> scenes = searchService.getScenesBy(params);
        assertThat(scenes, hasSize(5));
    }

    @Test
    public void testQueryByTimeliness() throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("timeliness", "Near Real Time");
        List<Scene> scenes = searchService.getScenesBy(params);
        assertThat(scenes, hasSize(10));
    }

    @Test
    public void testQueryByInstrument() throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("instrument", "OLCI");
        List<Scene> scenes = searchService.getScenesBy(params);
        assertThat(scenes, hasSize(10));
    }

    @Test
    public void testQueryByProductLevel() throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("productLevel", "L2");
        List<Scene> scenes = searchService.getScenesBy(params);
        assertThat(scenes, hasSize(1));
    }

    @Test
    public void testQueryByProcessingLevel() throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("processingLevel", "2LC");
        List<Scene> scenes = searchService.getScenesBy(params);
        assertThat(scenes, hasSize(1));
    }

    @Test
    public void testQueryByRelativeOrbitNumber() throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("relativeOrbitNumber", 9);
        List<Scene> scenes = searchService.getScenesBy(params);
        assertThat(scenes, hasSize(1));

        params = new HashMap<>();
        params.put("relativeOrbitNumber", "9");
        scenes = searchService.getScenesBy(params);
        assertThat(scenes, hasSize(1));

        params = new HashMap<>();
        params.put("relativeOrbitNumber", "09");
        scenes = searchService.getScenesBy(params);
        assertThat(scenes, hasSize(0));
    }

    @Test
    public void testQueryByAbsoluteOrbitNumber() throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("absoluteOrbitNumber", 9);
        List<Scene> scenes = searchService.getScenesBy(params);
        assertThat(scenes, hasSize(0));

        params = new HashMap<>();
        params.put("absoluteOrbitNumber", "09");
        scenes = searchService.getScenesBy(params);
        assertThat(scenes, hasSize(1));
    }

    @Test
    public void multipleSearch() throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("relativeOrbitNumber", 2);
        params.put("processingLevel", "2LC");
        params.put("cloudCover", 0.5f);
        params.put("polarisation", "VV VH");
        params.put("productType", "GRDH");
        params.put("satellitePlatform", "Sentinel-1A");
        params.put("sensingFrom", "2019-11-01T00:00:00.000000+00:00");
        params.put("sensingTo", "2019-11-12T00:00:00.000000+00:00");
        params.put("ingestionFrom", "2019-11-01T00:00:00.000000+00:00");
        params.put("ingestionTo", "2019-11-12T00:00:00.000000+00:00");
        params.put("order", "DESC");
        params.put("sortBy", "sensingTime");
        params.put("limit", 15);
        List<Scene> scenes = searchService.getScenesBy(params);
        assertThat(scenes, hasSize(1));
    }

    @Test
    public void testSqlInjection() throws SQLException {
        Map<String, Object> params = new HashMap<>();
        params.put("relativeOrbitNumber", 2);
        params.put("polarisation", "Dual VV VH\'UNION SELECT username, password FROM users--");
        params.put("order", "DESC");
        params.put("sortBy", "ingestionTime");
        params.put("limit", 15);
        params.put("offset", 1);
        List<Scene> scenes = searchService.getScenesBy(params);
        assertThat(scenes, hasSize(0));
    }
}
