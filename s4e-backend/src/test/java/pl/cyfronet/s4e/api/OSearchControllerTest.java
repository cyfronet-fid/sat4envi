package pl.cyfronet.s4e.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import pl.cyfronet.s4e.BasicTest;
import pl.cyfronet.s4e.SceneTestHelper;
import pl.cyfronet.s4e.TestDbHelper;
import pl.cyfronet.s4e.bean.AppUser;
import pl.cyfronet.s4e.bean.Product;
import pl.cyfronet.s4e.bean.Scene;
import pl.cyfronet.s4e.data.repository.AppUserRepository;
import pl.cyfronet.s4e.data.repository.ProductRepository;
import pl.cyfronet.s4e.data.repository.SceneRepository;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.reset;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static pl.cyfronet.s4e.Constants.API_PREFIX_V1;
import static pl.cyfronet.s4e.SceneTestHelper.productBuilder;
import static pl.cyfronet.s4e.TestJwtUtil.jwtBearerToken;

@AutoConfigureMockMvc
@BasicTest
@Slf4j
public class OSearchControllerTest {
    public static final String PROFILE_EMAIL = "get@profile.com";

    @Autowired
    private AppUserRepository appUserRepository;
    @Autowired
    private SceneRepository sceneRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private TestDbHelper testDbHelper;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private S3Presigner s3Presigner;
    @Autowired
    private MockMvc mockMvc;
    private AppUser appUser;

    @BeforeEach
    public void setUp() throws Exception {
        reset(s3Presigner);
        testDbHelper.clean();
        //add product
        Product product = productRepository.save(productBuilder().build());
        //addscenewithmetadata
        List<Scene> scenes = new ArrayList<>();
        for (long j = 0; j < 30; j++) {
            scenes.add(buildScene(product, j));
        }
        sceneRepository.saveAll(scenes);
        appUser = appUserRepository.save(AppUser.builder()
                .email(PROFILE_EMAIL)
                .name("Get")
                .surname("Profile")
                .password("{noop}password")
                .enabled(true)
                .build());
    }

    @AfterEach
    public void tearDown() {
        testDbHelper.clean();
    }

    private Scene buildScene(Product product, long number) throws Exception {
        JsonNode jsonNode = objectMapper.readTree(SceneTestHelper.getMetaDataWithNumber(number));
        Scene scene = SceneTestHelper.sceneWithMetadataBuilder(product, jsonNode)
                .build();
        scene.setSceneContent(objectMapper.readTree(SceneTestHelper.getSceneContent()));
        return scene;
    }

    @Nested
    class Search {
        @Test
        public void shouldReturnScenesDhus() throws Exception {
            mockMvc.perform(get(API_PREFIX_V1 + "/dhus/search")
                    .param("rows", "5")
                    .param("orderby", "beginposition asc")
                    .with(jwtBearerToken(appUser, objectMapper)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()", is(equalTo(5))));

            mockMvc.perform(get(API_PREFIX_V1 + "/dhus/search")
                    .param("rows", "10")
                    .param("orderby", "beginposition asc")
                    .with(jwtBearerToken(appUser, objectMapper)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()", is(equalTo(10))));

            mockMvc.perform(get(API_PREFIX_V1 + "/dhus/search")
                    .param("orderby", "beginposition asc")
                    .with(jwtBearerToken(appUser, objectMapper)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()", is(equalTo(20))));

            mockMvc.perform(get(API_PREFIX_V1 + "/dhus/search")
                    .param("start", "20")
                    .param("orderby", "beginposition asc")
                    .with(jwtBearerToken(appUser, objectMapper)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()", is(equalTo(10))));
        }

        @Test
        public void shouldReturnScenesDhusForMultipleQueryParams() throws Exception {
            mockMvc.perform(get(API_PREFIX_V1 + "/dhus/search")
                    .param("q", "platformname:Sentinel-1A AND sensoroperationalmode:IW")
                    .with(jwtBearerToken(appUser, objectMapper)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()", is(equalTo(20))));
            mockMvc.perform(get(API_PREFIX_V1 + "/dhus/search")
                    //yyyy-MM-ddThh:mm:ss.SSSZ
                    .param("q", "sensoroperationalmode:IW AND beginposition:[2019-11-01T00:00:00.000Z TO 2019-11-12T00:00:00.000Z]")
                    .with(jwtBearerToken(appUser, objectMapper)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()", is(equalTo(20))));
            mockMvc.perform(get(API_PREFIX_V1 + "/dhus/search")
                    .param("q", "sensoroperationalmode:IW AND beginposition:[NOW-24MONTHS TO NOW]")
                    .with(jwtBearerToken(appUser, objectMapper)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()", is(equalTo(20))));
        }

        @Test
        public void shouldReturnScenesDhusQueryPlatforname() throws Exception {
            //TODO: specific Sentinel not 1 or 2
            mockMvc.perform(get(API_PREFIX_V1 + "/dhus/search")
                    .param("q", "platformname:Sentinel-1A")
                    .with(jwtBearerToken(appUser, objectMapper)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()", is(equalTo(20))));

            mockMvc.perform(get(API_PREFIX_V1 + "/dhus/search")
                    .param("q", "platformname:Sentinel-1")
                    .with(jwtBearerToken(appUser, objectMapper)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()", is(equalTo(0))));
        }

        @Test
        public void shouldReturnScenesDhusQueryByTime() throws Exception {
            mockMvc.perform(get(API_PREFIX_V1 + "/dhus/search")
                    .param("q", "(beginposition:[2019-11-09T00:00:00.000000+00:00 TO 2019-11-12T00:00:00.000000+00:00])")
                    .with(jwtBearerToken(appUser, objectMapper)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()", is(equalTo(3))));

            mockMvc.perform(get(API_PREFIX_V1 + "/dhus/search")
                    .param("q", "(endposition:[2019-11-09T00:00:00.000000+00:00 TO 2019-11-12T00:00:00.000000+00:00])")
                    .with(jwtBearerToken(appUser, objectMapper)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()", is(equalTo(3))));

            mockMvc.perform(get(API_PREFIX_V1 + "/dhus/search")
                    .param("q", "(ingestiondate:[2019-11-09T00:00:00.000Z TO 2019-11-12T00:00:00.000Z])")
                    .with(jwtBearerToken(appUser, objectMapper)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()", is(equalTo(3))));

            mockMvc.perform(get(API_PREFIX_V1 + "/dhus/search")
                    .param("q", "(endposition:[NOW-24MONTHS TO NOW])")
                    .with(jwtBearerToken(appUser, objectMapper)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()", is(equalTo(20))));

            mockMvc.perform(get(API_PREFIX_V1 + "/dhus/search")
                    .param("q", "(endposition:[NOW-1DAY TO NOW-1HOUR])")
                    .with(jwtBearerToken(appUser, objectMapper)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()", is(equalTo(0))));

            mockMvc.perform(get(API_PREFIX_V1 + "/dhus/search")
                    .param("q", "(endposition:[NOW-5HOURS TO NOW-20MINUTES])")
                    .with(jwtBearerToken(appUser, objectMapper)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()", is(equalTo(0))));
        }

        @Test
        public void shouldntReturnScenesDhusQueryByTime() throws Exception {
            mockMvc.perform(get(API_PREFIX_V1 + "/dhus/search")
                    .param("q", "(beginposition:[2019-11-09T00:00:00.000000+00:00])")
                    .with(jwtBearerToken(appUser, objectMapper)))
                    .andExpect(status().isBadRequest());

            mockMvc.perform(get(API_PREFIX_V1 + "/dhus/search")
                    .param("q", "(beginposition:[2019-11-09T00:00:00.000000+00:00 TO])")
                    .with(jwtBearerToken(appUser, objectMapper)))
                    .andExpect(status().isBadRequest());

            mockMvc.perform(get(API_PREFIX_V1 + "/dhus/search")
                    .param("q", "(beginposition:[ TO 2019-11-12T00:00:00.000000+00:00])")
                    .with(jwtBearerToken(appUser, objectMapper)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        public void shouldReturnScenesDhusQueryCollection() throws Exception {
            mockMvc.perform(get(API_PREFIX_V1 + "/dhus/search")
                    .param("q", "collection:S1B_24AU")
                    .with(jwtBearerToken(appUser, objectMapper)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()", is(equalTo(20))));

            mockMvc.perform(get(API_PREFIX_V1 + "/dhus/search")
                    .param("q", "collection:not-exist")
                    .with(jwtBearerToken(appUser, objectMapper)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()", is(equalTo(0))));
        }

        @Test
        public void shouldReturnScenesDhusQueryFootprintPolygon() throws Exception {
            mockMvc.perform(get(API_PREFIX_V1 + "/dhus/search")
                    .param("q", "footprint:\"Intersects(POLYGON((155.90 19.60,156.60 15.70,157.40 16.10,157.34 20.05,155.90 19.60)))\"")
                    .with(jwtBearerToken(appUser, objectMapper)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()", is(equalTo(0))));

            mockMvc.perform(get(API_PREFIX_V1 + "/dhus/search")
                    .param("q", "footprint:\"Intersects(POLYGON((55.90 19.60,56.60 15.70,57.40 16.10,57.34 20.05,55.90 19.60)))\"")
                    .with(jwtBearerToken(appUser, objectMapper)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()", is(equalTo(20))));
        }

        @Test
        public void shouldReturnScenesDhusQueryFootprintPoint() throws Exception {
            mockMvc.perform(get(API_PREFIX_V1 + "/dhus/search")
                    .param("q", "footprint:\"Intersects(55.8000 19.5000)\"")
                    .with(jwtBearerToken(appUser, objectMapper)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()", is(equalTo(20))));

            mockMvc.perform(get(API_PREFIX_V1 + "/dhus/search")
                    .param("q", "footprint:\"Intersects(76.8000,17.0000)\"")
                    .with(jwtBearerToken(appUser, objectMapper)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()", is(equalTo(0))));
        }

        @Test
        public void shouldReturnScenesDhusQueryOrbitNumber() throws Exception {
            // TODO: key:[from TO to] e.g. key:[0 TO 50]
            mockMvc.perform(get(API_PREFIX_V1 + "/dhus/search")
                    .param("q", "orbitnumber:05")
                    .with(jwtBearerToken(appUser, objectMapper)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()", is(equalTo(1))));

            mockMvc.perform(get(API_PREFIX_V1 + "/dhus/search")
                    .param("q", "orbitnumber:5")
                    .with(jwtBearerToken(appUser, objectMapper)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()", is(equalTo(0))));

            mockMvc.perform(get(API_PREFIX_V1 + "/dhus/search")
                    .param("q", "orbitnumber:not-exist")
                    .with(jwtBearerToken(appUser, objectMapper)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()", is(equalTo(0))));

//        mockMvc.perform(get(API_PREFIX_V1 + "/dhus/search")
//                .param("q", "orbitnumber:[05 TO 07]"))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.length()", is(equalTo(5))));
        }


        @Test
        public void shouldReturnScenesDhusQueryLastOrbitNumber() throws Exception {
            // TODO key:[from TO to] e.g. key:[0 TO 50]
            mockMvc.perform(get(API_PREFIX_V1 + "/dhus/search")
                    .param("q", "lastorbitnumber:05")
                    .with(jwtBearerToken(appUser, objectMapper)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()", is(equalTo(1))));

            mockMvc.perform(get(API_PREFIX_V1 + "/dhus/search")
                    .param("q", "lastorbitnumber:5")
                    .with(jwtBearerToken(appUser, objectMapper)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()", is(equalTo(0))));

            mockMvc.perform(get(API_PREFIX_V1 + "/dhus/search")
                    .param("q", "lastorbitnumber:not-exist")
                    .with(jwtBearerToken(appUser, objectMapper)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()", is(equalTo(0))));

//        mockMvc.perform(get(API_PREFIX_V1 + "/dhus/search")
//                .param("q", "lastorbitnumber:[05 TO 07]"))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.length()", is(equalTo(5))));
        }

        @Test
        public void shouldReturnScenesDhusQueryRelativeOrbitNumber() throws Exception {
            // TODO: key:[from TO to] e.g. key:[0 TO 50]
            mockMvc.perform(get(API_PREFIX_V1 + "/dhus/search")
                    .param("q", "relativeorbitnumber:5")
                    .with(jwtBearerToken(appUser, objectMapper)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()", is(equalTo(1))));

            mockMvc.perform(get(API_PREFIX_V1 + "/dhus/search")
                    .param("q", "relativeorbitnumber:05")
                    .with(jwtBearerToken(appUser, objectMapper)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()", is(equalTo(0))));

            mockMvc.perform(get(API_PREFIX_V1 + "/dhus/search")
                    .param("q", "relativeorbitnumber:not-exist")
                    .with(jwtBearerToken(appUser, objectMapper)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()", is(equalTo(0))));

//        mockMvc.perform(get(API_PREFIX_V1 + "/dhus/search")
//                .param("q", "relativeorbitnumber:[05 TO 07]"))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.length()", is(equalTo(5))));
        }

        @Test
        public void shouldReturnScenesDhusQueryLastRelativeOrbitNumber() throws Exception {
            // TODO: key:[from TO to] e.g. key:[0 TO 50]
            mockMvc.perform(get(API_PREFIX_V1 + "/dhus/search")
                    .param("q", "lastrelativeorbitnumber:05")
                    .with(jwtBearerToken(appUser, objectMapper)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()", is(equalTo(0))));

            mockMvc.perform(get(API_PREFIX_V1 + "/dhus/search")
                    .param("q", "lastrelativeorbitnumber:5")
                    .with(jwtBearerToken(appUser, objectMapper)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()", is(equalTo(1))));

            mockMvc.perform(get(API_PREFIX_V1 + "/dhus/search")
                    .param("q", "lastrelativeorbitnumber:not-exist")
                    .with(jwtBearerToken(appUser, objectMapper)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()", is(equalTo(0))));

//        mockMvc.perform(get(API_PREFIX_V1 + "/dhus/search")
//                .param("q", "lastrelativeorbitnumber:[05 TO 07]"))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.length()", is(equalTo(5))));
        }

        @Test
        public void shouldReturnScenesDhusQueryPolarisation() throws Exception {
            mockMvc.perform(get(API_PREFIX_V1 + "/dhus/search")
                    .param("q", "polarisationmode:VV  VH")
                    .with(jwtBearerToken(appUser, objectMapper)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()", is(equalTo(20))));

            mockMvc.perform(get(API_PREFIX_V1 + "/dhus/search")
                    .param("q", "polarisationmode:VV +  +  VH")
                    .with(jwtBearerToken(appUser, objectMapper)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()", is(equalTo(20))));

            mockMvc.perform(get(API_PREFIX_V1 + "/dhus/search")
                    .param("q", "polarisationmode:VV")
                    .with(jwtBearerToken(appUser, objectMapper)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()", is(equalTo(0))));

            mockMvc.perform(get(API_PREFIX_V1 + "/dhus/search")
                    .param("q", "polarisationmode:not-exist")
                    .with(jwtBearerToken(appUser, objectMapper)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()", is(equalTo(0))));
        }

        @Test
        public void shouldReturnScenesDhusQueryProductType() throws Exception {
            mockMvc.perform(get(API_PREFIX_V1 + "/dhus/search")
                    .param("q", "producttype:GRDH")
                    .with(jwtBearerToken(appUser, objectMapper)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()", is(equalTo(20))));

            mockMvc.perform(get(API_PREFIX_V1 + "/dhus/search")
                    .param("q", "producttype:not-exist")
                    .with(jwtBearerToken(appUser, objectMapper)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()", is(equalTo(0))));
        }

        @Test
        public void shouldReturnScenesDhusQuerySensorOperationalMode() throws Exception {
            mockMvc.perform(get(API_PREFIX_V1 + "/dhus/search")
                    .param("q", "sensoroperationalmode:IW")
                    .with(jwtBearerToken(appUser, objectMapper)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()", is(equalTo(20))));

            mockMvc.perform(get(API_PREFIX_V1 + "/dhus/search")
                    .param("q", "sensoroperationalmode:not-exist")
                    .with(jwtBearerToken(appUser, objectMapper)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()", is(equalTo(0))));
        }

        @Test
        public void shouldReturnScenesDhusQueryCloudCoverage() throws Exception {
            // TODO: key:[80 TO 90]
            mockMvc.perform(get(API_PREFIX_V1 + "/dhus/search")
                    .param("q", "cloudcoveragepercentage:15")
                    .with(jwtBearerToken(appUser, objectMapper)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()", is(equalTo(16))));

            mockMvc.perform(get(API_PREFIX_V1 + "/dhus/search")
                    .param("q", "cloudcoveragepercentage:0")
                    .with(jwtBearerToken(appUser, objectMapper)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()", is(equalTo(1))));

//        mockMvc.perform(get(API_PREFIX_V1 + "/dhus/search")
//                .param("q", "cloudcoveragepercentage:[80 TO 90]"))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.length()", is(equalTo(5))));
        }

        @Test
        public void shouldReturnScenesDhusQueryTimeliness() throws Exception {
            mockMvc.perform(get(API_PREFIX_V1 + "/dhus/search")
                    .param("q", "timeliness:Near Real Time")
                    .with(jwtBearerToken(appUser, objectMapper)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()", is(equalTo(20))));

            mockMvc.perform(get(API_PREFIX_V1 + "/dhus/search")
                    .param("q", "timeliness:Near Real Time2")
                    .with(jwtBearerToken(appUser, objectMapper)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()", is(equalTo(0))));
        }
    }

    @Nested
    class Count {
        @Test
        public void shouldReturnCountDhus() throws Exception {
            mockMvc.perform(get(API_PREFIX_V1 + "/dhus/search/count")
                    .param("rows", "5")
                    .param("orderby", "beginposition asc")
                    .with(jwtBearerToken(appUser, objectMapper)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", is(equalTo(30))));

            mockMvc.perform(get(API_PREFIX_V1 + "/dhus/search/count")
                    .param("rows", "10")
                    .param("orderby", "beginposition asc")
                    .with(jwtBearerToken(appUser, objectMapper)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", is(equalTo(30))));

            mockMvc.perform(get(API_PREFIX_V1 + "/dhus/search/count")
                    .param("orderby", "beginposition asc")
                    .with(jwtBearerToken(appUser, objectMapper)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", is(equalTo(30))));

            mockMvc.perform(get(API_PREFIX_V1 + "/dhus/search/count")
                    .param("start", "20")
                    .param("orderby", "beginposition asc")
                    .with(jwtBearerToken(appUser, objectMapper)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", is(equalTo(30))));
        }

        @Test
        public void shouldReturnScenesDhusForMultipleQueryParams() throws Exception {
            mockMvc.perform(get(API_PREFIX_V1 + "/dhus/search/count")
                    .param("q", "platformname:Sentinel-1A AND sensoroperationalmode:IW")
                    .with(jwtBearerToken(appUser, objectMapper)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", is(equalTo(30))));
            mockMvc.perform(get(API_PREFIX_V1 + "/dhus/search/count")
                    //yyyy-MM-ddThh:mm:ss.SSSZ
                    .param("q", "sensoroperationalmode:IW AND beginposition:[2019-11-01T00:00:00.000Z TO 2019-11-12T00:00:00.000Z]")
                    .with(jwtBearerToken(appUser, objectMapper)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", is(equalTo(30))));
            mockMvc.perform(get(API_PREFIX_V1 + "/dhus/search/count")
                    .param("q", "sensoroperationalmode:IW AND beginposition:[NOW-24MONTHS TO NOW]")
                    .with(jwtBearerToken(appUser, objectMapper)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", is(equalTo(30))));
        }

        @Test
        public void shouldReturnScenesDhusQueryPlatforname() throws Exception {
            //TODO: specific Sentinel not 1 or 2
            mockMvc.perform(get(API_PREFIX_V1 + "/dhus/search/count")
                    .param("q", "platformname:Sentinel-1A")
                    .with(jwtBearerToken(appUser, objectMapper)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", is(equalTo(30))));

            mockMvc.perform(get(API_PREFIX_V1 + "/dhus/search/count")
                    .param("q", "platformname:Sentinel-1")
                    .with(jwtBearerToken(appUser, objectMapper)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", is(equalTo(0))));
        }

        @Test
        public void shouldReturnScenesDhusQueryByTime() throws Exception {
            mockMvc.perform(get(API_PREFIX_V1 + "/dhus/search/count")
                    .param("q", "(beginposition:[2019-11-09T00:00:00.000000+00:00 TO 2019-11-12T00:00:00.000000+00:00])")
                    .with(jwtBearerToken(appUser, objectMapper)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", is(equalTo(3))));

            mockMvc.perform(get(API_PREFIX_V1 + "/dhus/search/count")
                    .param("q", "(endposition:[2019-11-09T00:00:00.000000+00:00 TO 2019-11-12T00:00:00.000000+00:00])")
                    .with(jwtBearerToken(appUser, objectMapper)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", is(equalTo(3))));

            mockMvc.perform(get(API_PREFIX_V1 + "/dhus/search/count")
                    .param("q", "(ingestiondate:[2019-11-09T00:00:00.000Z TO 2019-11-12T00:00:00.000Z])")
                    .with(jwtBearerToken(appUser, objectMapper)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", is(equalTo(3))));

            mockMvc.perform(get(API_PREFIX_V1 + "/dhus/search/count")
                    .param("q", "(endposition:[NOW-24MONTHS TO NOW])")
                    .with(jwtBearerToken(appUser, objectMapper)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", is(equalTo(30))));

            mockMvc.perform(get(API_PREFIX_V1 + "/dhus/search/count")
                    .param("q", "(endposition:[NOW-1DAY TO NOW-1HOUR])")
                    .with(jwtBearerToken(appUser, objectMapper)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", is(equalTo(0))));

            mockMvc.perform(get(API_PREFIX_V1 + "/dhus/search/count")
                    .param("q", "(endposition:[NOW-5HOURS TO NOW-20MINUTES])")
                    .with(jwtBearerToken(appUser, objectMapper)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", is(equalTo(0))));
        }

        @Test
        public void shouldReturnScenesDhusQueryCollection() throws Exception {
            mockMvc.perform(get(API_PREFIX_V1 + "/dhus/search/count")
                    .param("q", "collection:S1B_24AU")
                    .with(jwtBearerToken(appUser, objectMapper)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", is(equalTo(30))));

            mockMvc.perform(get(API_PREFIX_V1 + "/dhus/search/count")
                    .param("q", "collection:not-exist")
                    .with(jwtBearerToken(appUser, objectMapper)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", is(equalTo(0))));
        }

        @Test
        public void shouldReturnScenesDhusQueryFootprintPolygon() throws Exception {
            mockMvc.perform(get(API_PREFIX_V1 + "/dhus/search/count")
                    .param("q", "footprint:\"Intersects(POLYGON((155.90 19.60,156.60 15.70,157.40 16.10,157.34 20.05,155.90 19.60)))\"")
                    .with(jwtBearerToken(appUser, objectMapper)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", is(equalTo(0))));

            mockMvc.perform(get(API_PREFIX_V1 + "/dhus/search/count")
                    .param("q", "footprint:\"Intersects(POLYGON((55.90 19.60,56.60 15.70,57.40 16.10,57.34 20.05,55.90 19.60)))\"")
                    .with(jwtBearerToken(appUser, objectMapper)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", is(equalTo(30))));
        }

        @Test
        public void shouldReturnScenesDhusQueryFootprintPoint() throws Exception {
            mockMvc.perform(get(API_PREFIX_V1 + "/dhus/search/count")
                    .param("q", "footprint:\"Intersects(55.8000 19.5000)\"")
                    .with(jwtBearerToken(appUser, objectMapper)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", is(equalTo(30))));

            mockMvc.perform(get(API_PREFIX_V1 + "/dhus/search/count")
                    .param("q", "footprint:\"Intersects(76.8000,17.0000)\"")
                    .with(jwtBearerToken(appUser, objectMapper)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", is(equalTo(0))));
        }

        @Test
        public void shouldReturnScenesDhusQueryOrbitNumber() throws Exception {
            // TODO: key:[from TO to] e.g. key:[0 TO 50]
            mockMvc.perform(get(API_PREFIX_V1 + "/dhus/search/count")
                    .param("q", "orbitnumber:05")
                    .with(jwtBearerToken(appUser, objectMapper)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", is(equalTo(1))));

            mockMvc.perform(get(API_PREFIX_V1 + "/dhus/search/count")
                    .param("q", "orbitnumber:5")
                    .with(jwtBearerToken(appUser, objectMapper)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", is(equalTo(0))));

            mockMvc.perform(get(API_PREFIX_V1 + "/dhus/search/count")
                    .param("q", "orbitnumber:not-exist")
                    .with(jwtBearerToken(appUser, objectMapper)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", is(equalTo(0))));
        }


        @Test
        public void shouldReturnScenesDhusQueryLastOrbitNumber() throws Exception {
            // TODO key:[from TO to] e.g. key:[0 TO 50]
            mockMvc.perform(get(API_PREFIX_V1 + "/dhus/search/count")
                    .param("q", "lastorbitnumber:05")
                    .with(jwtBearerToken(appUser, objectMapper)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", is(equalTo(1))));

            mockMvc.perform(get(API_PREFIX_V1 + "/dhus/search/count")
                    .param("q", "lastorbitnumber:5")
                    .with(jwtBearerToken(appUser, objectMapper)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", is(equalTo(0))));

            mockMvc.perform(get(API_PREFIX_V1 + "/dhus/search/count")
                    .param("q", "lastorbitnumber:not-exist")
                    .with(jwtBearerToken(appUser, objectMapper)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", is(equalTo(0))));
        }

        @Test
        public void shouldReturnScenesDhusQueryRelativeOrbitNumber() throws Exception {
            // TODO: key:[from TO to] e.g. key:[0 TO 50]
            mockMvc.perform(get(API_PREFIX_V1 + "/dhus/search/count")
                    .param("q", "relativeorbitnumber:5")
                    .with(jwtBearerToken(appUser, objectMapper)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", is(equalTo(1))));

            mockMvc.perform(get(API_PREFIX_V1 + "/dhus/search/count")
                    .param("q", "relativeorbitnumber:05")
                    .with(jwtBearerToken(appUser, objectMapper)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", is(equalTo(0))));

            mockMvc.perform(get(API_PREFIX_V1 + "/dhus/search/count")
                    .param("q", "relativeorbitnumber:not-exist")
                    .with(jwtBearerToken(appUser, objectMapper)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", is(equalTo(0))));
        }

        @Test
        public void shouldReturnScenesDhusQueryLastRelativeOrbitNumber() throws Exception {
            // TODO: key:[from TO to] e.g. key:[0 TO 50]
            mockMvc.perform(get(API_PREFIX_V1 + "/dhus/search/count")
                    .param("q", "lastrelativeorbitnumber:05")
                    .with(jwtBearerToken(appUser, objectMapper)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", is(equalTo(0))));

            mockMvc.perform(get(API_PREFIX_V1 + "/dhus/search/count")
                    .param("q", "lastrelativeorbitnumber:5")
                    .with(jwtBearerToken(appUser, objectMapper)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", is(equalTo(1))));

            mockMvc.perform(get(API_PREFIX_V1 + "/dhus/search/count")
                    .param("q", "lastrelativeorbitnumber:not-exist")
                    .with(jwtBearerToken(appUser, objectMapper)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", is(equalTo(0))));
        }

        @Test
        public void shouldReturnScenesDhusQueryPolarisation() throws Exception {
            mockMvc.perform(get(API_PREFIX_V1 + "/dhus/search/count")
                    .param("q", "polarisationmode:VV  VH")
                    .with(jwtBearerToken(appUser, objectMapper)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", is(equalTo(30))));

            mockMvc.perform(get(API_PREFIX_V1 + "/dhus/search/count")
                    .param("q", "polarisationmode:VV +  +  VH")
                    .with(jwtBearerToken(appUser, objectMapper)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", is(equalTo(30))));

            mockMvc.perform(get(API_PREFIX_V1 + "/dhus/search/count")
                    .param("q", "polarisationmode:VV")
                    .with(jwtBearerToken(appUser, objectMapper)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", is(equalTo(0))));

            mockMvc.perform(get(API_PREFIX_V1 + "/dhus/search/count")
                    .param("q", "polarisationmode:not-exist")
                    .with(jwtBearerToken(appUser, objectMapper)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", is(equalTo(0))));
        }

        @Test
        public void shouldReturnScenesDhusQueryProductType() throws Exception {
            mockMvc.perform(get(API_PREFIX_V1 + "/dhus/search/count")
                    .param("q", "producttype:GRDH")
                    .with(jwtBearerToken(appUser, objectMapper)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", is(equalTo(30))));

            mockMvc.perform(get(API_PREFIX_V1 + "/dhus/search/count")
                    .param("q", "producttype:not-exist")
                    .with(jwtBearerToken(appUser, objectMapper)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", is(equalTo(0))));
        }

        @Test
        public void shouldReturnScenesDhusQuerySensorOperationalMode() throws Exception {
            mockMvc.perform(get(API_PREFIX_V1 + "/dhus/search/count")
                    .param("q", "sensoroperationalmode:IW")
                    .with(jwtBearerToken(appUser, objectMapper)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", is(equalTo(30))));

            mockMvc.perform(get(API_PREFIX_V1 + "/dhus/search/count")
                    .param("q", "sensoroperationalmode:not-exist")
                    .with(jwtBearerToken(appUser, objectMapper)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", is(equalTo(0))));
        }

        @Test
        public void shouldReturnScenesDhusQueryCloudCoverage() throws Exception {
            // TODO: key:[80 TO 90]
            mockMvc.perform(get(API_PREFIX_V1 + "/dhus/search/count")
                    .param("q", "cloudcoveragepercentage:15")
                    .with(jwtBearerToken(appUser, objectMapper)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", is(equalTo(16))));

            mockMvc.perform(get(API_PREFIX_V1 + "/dhus/search/count")
                    .param("q", "cloudcoveragepercentage:0")
                    .with(jwtBearerToken(appUser, objectMapper)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", is(equalTo(1))));
        }

        @Test
        public void shouldReturnScenesDhusQueryTimeliness() throws Exception {
            mockMvc.perform(get(API_PREFIX_V1 + "/dhus/search/count")
                    .param("q", "timeliness:Near Real Time")
                    .with(jwtBearerToken(appUser, objectMapper)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", is(equalTo(30))));

            mockMvc.perform(get(API_PREFIX_V1 + "/dhus/search/count")
                    .param("q", "timeliness:Near Real Time2")
                    .with(jwtBearerToken(appUser, objectMapper)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", is(equalTo(0))));
        }
    }

    @Test
    public void shouldReturnScenesDhusQueryFileName() throws Exception {
        // TODO but how?
    }

    @Test
    public void shouldReturnScenesDhusQueryOrbitDirection() throws Exception {
        // TODO but how?
    }

    @Test
    public void shouldReturnScenesDhusQuerySwathIdentifier() throws Exception {
        // TODO but how?
    }
}
