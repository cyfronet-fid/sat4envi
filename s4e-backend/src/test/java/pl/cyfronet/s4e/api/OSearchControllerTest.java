/*
 * Copyright 2021 ACC Cyfronet AGH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package pl.cyfronet.s4e.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
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

import java.time.Clock;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.reset;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static pl.cyfronet.s4e.Constants.API_PREFIX_V1;
import static pl.cyfronet.s4e.SceneTestHelper.productBuilder;
import static pl.cyfronet.s4e.SceneTestHelper.toScene;
import static pl.cyfronet.s4e.TestJwtUtil.jwtBearerToken;

@AutoConfigureMockMvc
@BasicTest
@Slf4j
public class OSearchControllerTest {
    public static final String PROFILE_EMAIL = "get@profile.com";
    @Autowired
    Clock clock;
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
    private Product product;

    @BeforeEach
    public void setUp() throws Exception {
        reset(s3Presigner);
        testDbHelper.clean();
        //add product
        product = productRepository.save(productBuilder().build());
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

    private Scene buildScene(Product product, long number) {
        JsonNode metadataContent = SceneTestHelper.getMetadataContentWithNumber(number);
        Scene scene = SceneTestHelper.sceneWithMetadataBuilder(product, metadataContent).build();
        scene.setSceneKey("path/to/" + number + ".scene");
        scene.setSceneContent(SceneTestHelper.getSceneContent());
        return scene;
    }

    @Nested
    class Search {
        @Test
        public void shouldReturnScenesByEumetsatLicense() throws Exception {
            val product = productRepository.save(productBuilder().accessType(Product.AccessType.EUMETSAT).build());

            val now = ZonedDateTime.now(clock).withZoneSameInstant(ZoneId.of("UTC"));
            val scenes = Stream.of(
                    now.withMinute(10).withSecond(0).withNano(0).toLocalDateTime(),
                    now.withMinute(10).withSecond(0).withNano(0).minusHours(1).toLocalDateTime(),
                    now.withMinute(0).withSecond(0).withNano(0).minusHours(2).toLocalDateTime(),
                    now.withMinute(0).withSecond(0).withNano(0).minusHours(3).toLocalDateTime(),
                    now.withMinute(10).withSecond(0).withNano(0).minusHours(4).toLocalDateTime()
            )
                    .map(toScene(product))
                    .map(sceneRepository::save)
                    .collect(Collectors.toList());

            mockMvc.perform(get(API_PREFIX_V1 + "/dhus/search")
                    .param("q", "producttype:" + product.getName())
                    .param("rows", "5")
                    .param("orderby", "beginposition asc")
                    .with(jwtBearerToken(appUser, objectMapper)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()", is(equalTo(3))))
                    .andExpect(jsonPath("$..id", contains(
                            equalTo(scenes.get(2).getId().intValue()),
                            equalTo(scenes.get(3).getId().intValue()),
                            equalTo(scenes.get(4).getId().intValue()))));
        }
        @Test
        public void shouldReturnScenesDhus() throws Exception {
            String productType = product.getName();
            mockMvc.perform(get(API_PREFIX_V1 + "/dhus/search")
                    .param("q", "producttype:" + product.getName())
                    .param("rows", "5")
                    .param("orderby", "beginposition asc")
                    .with(jwtBearerToken(appUser, objectMapper)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()", is(equalTo(5))));

            mockMvc.perform(get(API_PREFIX_V1 + "/dhus/search")
                    .param("q", "producttype:" + product.getName())
                    .param("rows", "10")
                    .param("orderby", "beginposition asc")
                    .with(jwtBearerToken(appUser, objectMapper)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()", is(equalTo(10))));

            mockMvc.perform(get(API_PREFIX_V1 + "/dhus/search")
                    .param("q", "producttype:" + product.getName())
                    .param("orderby", "beginposition asc")
                    .with(jwtBearerToken(appUser, objectMapper)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()", is(equalTo(20))));

            mockMvc.perform(get(API_PREFIX_V1 + "/dhus/search")
                    .param("q", "producttype:" + product.getName())
                    .param("start", "20")
                    .param("orderby", "beginposition asc")
                    .with(jwtBearerToken(appUser, objectMapper)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()", is(equalTo(10))));
        }

        @ParameterizedTest
        @CsvSource({
                "beginposition asc,    path/to/0.scene,  path/to/1.scene",
                "beginposition desc,   path/to/29.scene, path/to/28.scene",
                "ingestiondate asc,    path/to/0.scene,  path/to/1.scene",
                "ingestiondate desc,   path/to/29.scene, path/to/28.scene",
                // Default to desc ordering:
                "ingestiondate,        path/to/29.scene, path/to/28.scene",
                "ingestiondate desc q, path/to/29.scene, path/to/28.scene",
                "foo bar baz,          path/to/29.scene, path/to/28.scene",
                "foo,                  path/to/29.scene, path/to/28.scene",
                "foo bar,              path/to/29.scene, path/to/28.scene",
                ",                     path/to/29.scene, path/to/28.scene",
        })
        public void shouldApplyOrderBy(String orderBy, String sceneKey1, String sceneKey2) throws Exception {
            mockMvc.perform(get(API_PREFIX_V1 + "/dhus/search")
                    .param("q", "producttype:" + product.getName())
                    .param("rows", "2")
                    .param("orderby", orderBy)
                    .with(jwtBearerToken(appUser, objectMapper)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()", is(equalTo(2))))
                    .andExpect(jsonPath("$[0].sceneKey", is(equalTo(sceneKey1))))
                    .andExpect(jsonPath("$[1].sceneKey", is(equalTo(sceneKey2))));
        }

        @Test
        public void shouldReturnScenesDhusForMultipleQueryParams() throws Exception {
            mockMvc.perform(get(API_PREFIX_V1 + "/dhus/search")
                    .param("q", "platformname:Sentinel-1A AND sensoroperationalmode:IW " +
                            "AND producttype:" + product.getName())
                    .with(jwtBearerToken(appUser, objectMapper)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()", is(equalTo(20))));
            mockMvc.perform(get(API_PREFIX_V1 + "/dhus/search")
                    //yyyy-MM-ddThh:mm:ss.SSSZ
                    .param("q", "sensoroperationalmode:IW AND beginposition:[2019-11-01T00:00:00.000Z TO 2019-11-12T00:00:00.000Z] " +
                            "AND producttype:" + product.getName())
                    .with(jwtBearerToken(appUser, objectMapper)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()", is(equalTo(20))));
            mockMvc.perform(get(API_PREFIX_V1 + "/dhus/search")
                    .param("q", "sensoroperationalmode:IW AND beginposition:[NOW-24MONTHS TO NOW] " +
                            "AND producttype:" + product.getName())
                    .with(jwtBearerToken(appUser, objectMapper)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()", is(equalTo(20))));
        }

        @Test
        public void shouldReturnScenesDhusQueryPlatforname() throws Exception {
            //TODO: specific Sentinel not 1 or 2
            mockMvc.perform(get(API_PREFIX_V1 + "/dhus/search")
                    .param("q", "platformname:Sentinel-1A " +
                            "AND producttype:" + product.getName())
                    .with(jwtBearerToken(appUser, objectMapper)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()", is(equalTo(20))));

            mockMvc.perform(get(API_PREFIX_V1 + "/dhus/search")
                    .param("q", "platformname:Sentinel-1 " +
                            "AND producttype:" + product.getName())
                    .with(jwtBearerToken(appUser, objectMapper)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()", is(equalTo(0))));
        }

        @Test
        public void shouldReturnScenesDhusQueryByTime() throws Exception {
            mockMvc.perform(get(API_PREFIX_V1 + "/dhus/search")
                    .param("q", "beginposition:[2019-11-09T00:00:00.000000+00:00 TO 2019-11-12T00:00:00.000000+00:00] " +
                            "AND producttype:" + product.getName())
                    .with(jwtBearerToken(appUser, objectMapper)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()", is(equalTo(3))));

            mockMvc.perform(get(API_PREFIX_V1 + "/dhus/search")
                    .param("q", "endposition:[2019-11-09T00:00:00.000000+00:00 TO 2019-11-12T00:00:00.000000+00:00] " +
                            "AND producttype:" + product.getName())
                    .with(jwtBearerToken(appUser, objectMapper)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()", is(equalTo(3))));

            mockMvc.perform(get(API_PREFIX_V1 + "/dhus/search")
                    .param("q", "ingestiondate:[2019-11-09T00:00:00.000Z TO 2019-11-12T00:00:00.000Z] " +
                            "AND producttype:" + product.getName())
                    .with(jwtBearerToken(appUser, objectMapper)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()", is(equalTo(3))));

            mockMvc.perform(get(API_PREFIX_V1 + "/dhus/search")
                    .param("q", "endposition:[NOW-24MONTHS TO NOW] " +
                            "AND producttype:" + product.getName())
                    .with(jwtBearerToken(appUser, objectMapper)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()", is(equalTo(20))));

            mockMvc.perform(get(API_PREFIX_V1 + "/dhus/search")
                    .param("q", "endposition:[NOW-1DAY TO NOW-1HOUR] " +
                            "AND producttype:" + product.getName())
                    .with(jwtBearerToken(appUser, objectMapper)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()", is(equalTo(0))));

            mockMvc.perform(get(API_PREFIX_V1 + "/dhus/search")
                    .param("q", "endposition:[NOW-5HOURS TO NOW-20MINUTES] " +
                            "AND producttype:" + product.getName())
                    .with(jwtBearerToken(appUser, objectMapper)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()", is(equalTo(0))));
        }

        @Test
        public void shouldntReturnScenesDhusQueryByTime() throws Exception {
            mockMvc.perform(get(API_PREFIX_V1 + "/dhus/search")
                    .param("q", "beginposition:[2019-11-09T00:00:00.000000+00:00] " +
                            "AND producttype:" + product.getName())
                    .with(jwtBearerToken(appUser, objectMapper)))
                    .andExpect(status().isBadRequest());

            mockMvc.perform(get(API_PREFIX_V1 + "/dhus/search")
                    .param("q", "beginposition:[2019-11-09T00:00:00.000000+00:00 TO] " +
                            "AND producttype:" + product.getName())
                    .with(jwtBearerToken(appUser, objectMapper)))
                    .andExpect(status().isBadRequest());

            mockMvc.perform(get(API_PREFIX_V1 + "/dhus/search")
                    .param("q", "beginposition:[ TO 2019-11-12T00:00:00.000000+00:00] " +
                            "AND producttype:" + product.getName())
                    .with(jwtBearerToken(appUser, objectMapper)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        public void shouldReturnScenesDhusQueryCollection() throws Exception {
            mockMvc.perform(get(API_PREFIX_V1 + "/dhus/search")
                    .param("q", "collection:S1B_24AU " +
                            "AND producttype:" + product.getName())
                    .with(jwtBearerToken(appUser, objectMapper)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()", is(equalTo(20))));

            mockMvc.perform(get(API_PREFIX_V1 + "/dhus/search")
                    .param("q", "collection:not-exist " +
                            "AND producttype:" + product.getName())
                    .with(jwtBearerToken(appUser, objectMapper)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()", is(equalTo(0))));
        }

        @Test
        public void shouldReturnScenesDhusQueryFootprintPolygon() throws Exception {
            mockMvc.perform(get(API_PREFIX_V1 + "/dhus/search")
                    .param("q", "footprint:Intersects(POLYGON((155.90 19.60,156.60 15.70,157.40 16.10,157.34 20.05,155.90 19.60))) " +
                            "AND producttype:" + product.getName())
                    .with(jwtBearerToken(appUser, objectMapper)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()", is(equalTo(0))));

            mockMvc.perform(get(API_PREFIX_V1 + "/dhus/search")
                    .param("q", "footprint:Intersects(POLYGON((12.6 55,19 55.25,26 55,24.93 47.55,19 47.67,14 47.5,12.6 55))) " +
                            "AND producttype:" + product.getName())
                    .with(jwtBearerToken(appUser, objectMapper)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()", is(equalTo(20))));
        }

        @Test
        public void shouldReturnScenesDhusQueryFootprintPoint() throws Exception {
            mockMvc.perform(get(API_PREFIX_V1 + "/dhus/search")
                    .param("q", "footprint:Intersects(15 51) " +
                            "AND producttype:" + product.getName())
                    .with(jwtBearerToken(appUser, objectMapper)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()", is(equalTo(20))));

            mockMvc.perform(get(API_PREFIX_V1 + "/dhus/search")
                    .param("q", "footprint:Intersects(12 55) " +
                            "AND producttype:" + product.getName())
                    .with(jwtBearerToken(appUser, objectMapper)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()", is(equalTo(0))));
        }

        @Test
        public void shouldReturnScenesDhusQueryOrbitNumber() throws Exception {
            // TODO: key:[from TO to] e.g. key:[0 TO 50]
            mockMvc.perform(get(API_PREFIX_V1 + "/dhus/search")
                    .param("q", "orbitnumber:05 " +
                            "AND producttype:" + product.getName())
                    .with(jwtBearerToken(appUser, objectMapper)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()", is(equalTo(1))));

            mockMvc.perform(get(API_PREFIX_V1 + "/dhus/search")
                    .param("q", "orbitnumber:5 " +
                            "AND producttype:" + product.getName())
                    .with(jwtBearerToken(appUser, objectMapper)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()", is(equalTo(0))));

            mockMvc.perform(get(API_PREFIX_V1 + "/dhus/search")
                    .param("q", "orbitnumber:not-exist " +
                            "AND producttype:" + product.getName())
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
                    .param("q", "lastorbitnumber:05 " +
                            "AND producttype:" + product.getName())
                    .with(jwtBearerToken(appUser, objectMapper)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()", is(equalTo(1))));

            mockMvc.perform(get(API_PREFIX_V1 + "/dhus/search")
                    .param("q", "lastorbitnumber:5 " +
                            "AND producttype:" + product.getName())
                    .with(jwtBearerToken(appUser, objectMapper)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()", is(equalTo(0))));

            mockMvc.perform(get(API_PREFIX_V1 + "/dhus/search")
                    .param("q", "lastorbitnumber:not-exist " +
                            "AND producttype:" + product.getName())
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
                    .param("q", "relativeorbitnumber:5 " +
                            "AND producttype:" + product.getName())
                    .with(jwtBearerToken(appUser, objectMapper)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()", is(equalTo(1))));

            mockMvc.perform(get(API_PREFIX_V1 + "/dhus/search")
                    .param("q", "relativeorbitnumber:05 " +
                            "AND producttype:" + product.getName())
                    .with(jwtBearerToken(appUser, objectMapper)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()", is(equalTo(0))));

            mockMvc.perform(get(API_PREFIX_V1 + "/dhus/search")
                    .param("q", "relativeorbitnumber:not-exist " +
                            "AND producttype:" + product.getName())
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
                    .param("q", "lastrelativeorbitnumber:05 " +
                            "AND producttype:" + product.getName())
                    .with(jwtBearerToken(appUser, objectMapper)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()", is(equalTo(0))));

            mockMvc.perform(get(API_PREFIX_V1 + "/dhus/search")
                    .param("q", "lastrelativeorbitnumber:5 " +
                            "AND producttype:" + product.getName())
                    .with(jwtBearerToken(appUser, objectMapper)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()", is(equalTo(1))));

            mockMvc.perform(get(API_PREFIX_V1 + "/dhus/search")
                    .param("q", "lastrelativeorbitnumber:not-exist " +
                            "AND producttype:" + product.getName())
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
                    .param("q", "polarisationmode:VV  VH " +
                            "AND producttype:" + product.getName())
                    .with(jwtBearerToken(appUser, objectMapper)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()", is(equalTo(20))));

            mockMvc.perform(get(API_PREFIX_V1 + "/dhus/search")
                    .param("q", "polarisationmode:VV +  +  VH " +
                            "AND producttype:" + product.getName())
                    .with(jwtBearerToken(appUser, objectMapper)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()", is(equalTo(20))));

            mockMvc.perform(get(API_PREFIX_V1 + "/dhus/search")
                    .param("q", "polarisationmode:VV " +
                            "AND producttype:" + product.getName())
                    .with(jwtBearerToken(appUser, objectMapper)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()", is(equalTo(0))));

            mockMvc.perform(get(API_PREFIX_V1 + "/dhus/search")
                    .param("q", "polarisationmode:not-exist " +
                            "AND producttype:" + product.getName())
                    .with(jwtBearerToken(appUser, objectMapper)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()", is(equalTo(0))));
        }

        @Test
        public void shouldReturnScenesDhusQueryProductType() throws Exception {
            mockMvc.perform(get(API_PREFIX_V1 + "/dhus/search")
                    .param("q", "producttype:" + product.getName())
                    .with(jwtBearerToken(appUser, objectMapper)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()", is(equalTo(20))));

            mockMvc.perform(get(API_PREFIX_V1 + "/dhus/search")
                    .param("q", "producttype:not-exist")
                    .with(jwtBearerToken(appUser, objectMapper)))
                    .andExpect(status().isNotFound());
        }

        @Test
        public void shouldReturnScenesDhusQuerySensorOperationalMode() throws Exception {
            mockMvc.perform(get(API_PREFIX_V1 + "/dhus/search")
                    .param("q", "sensoroperationalmode:IW " +
                            "AND producttype:" + product.getName())
                    .with(jwtBearerToken(appUser, objectMapper)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()", is(equalTo(20))));

            mockMvc.perform(get(API_PREFIX_V1 + "/dhus/search")
                    .param("q", "sensoroperationalmode:not-exist " +
                            "AND producttype:" + product.getName())
                    .with(jwtBearerToken(appUser, objectMapper)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()", is(equalTo(0))));
        }

        @Test
        public void shouldReturnScenesDhusQueryCloudCoverage() throws Exception {
            // TODO: key:[80 TO 90]
            mockMvc.perform(get(API_PREFIX_V1 + "/dhus/search")
                    .param("q", "cloudcoveragepercentage:15 " +
                            "AND producttype:" + product.getName())
                    .with(jwtBearerToken(appUser, objectMapper)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()", is(equalTo(16))));

            mockMvc.perform(get(API_PREFIX_V1 + "/dhus/search")
                    .param("q", "cloudcoveragepercentage:0 " +
                            "AND producttype:" + product.getName())
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
                    .param("q", "timeliness:Near Real Time " +
                            "AND producttype:" + product.getName())
                    .with(jwtBearerToken(appUser, objectMapper)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()", is(equalTo(20))));

            mockMvc.perform(get(API_PREFIX_V1 + "/dhus/search")
                    .param("q", "timeliness:Near Real Time2 " +
                            "AND producttype:" + product.getName())
                    .with(jwtBearerToken(appUser, objectMapper)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()", is(equalTo(0))));
        }
    }

    @Nested
    class Count {
        @Test
        public void shouldReturnCountByEumetsatLicenseScenes() throws Exception {
            val product = productRepository.save(productBuilder().accessType(Product.AccessType.EUMETSAT).build());

            val now = ZonedDateTime.now(clock).withZoneSameInstant(ZoneId.of("UTC"));
            val scenes = Stream.of(
                    now.withMinute(10).withSecond(0).withNano(0).toLocalDateTime(),
                    now.withMinute(10).withSecond(0).withNano(0).minusHours(1).toLocalDateTime(),
                    now.withMinute(0).withSecond(0).withNano(0).minusHours(2).toLocalDateTime(),
                    now.withMinute(0).withSecond(0).withNano(0).minusHours(3).toLocalDateTime(),
                    now.withMinute(10).withSecond(0).withNano(0).minusHours(4).toLocalDateTime()
            )
                    .map(toScene(product))
                    .map(sceneRepository::save)
                    .collect(Collectors.toList());

            mockMvc.perform(get(API_PREFIX_V1 + "/dhus/search/count")
                    .param("q", "producttype:" + product.getName())
                    .param("rows", "5")
                    .param("orderby", "beginposition asc")
                    .with(jwtBearerToken(appUser, objectMapper)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", is(equalTo(3))));
        }

        @Test
        public void shouldReturnCountDhus() throws Exception {
            mockMvc.perform(get(API_PREFIX_V1 + "/dhus/search/count")
                    .param("q", "producttype:" + product.getName())
                    .param("rows", "5")
                    .param("orderby", "beginposition asc")
                    .with(jwtBearerToken(appUser, objectMapper)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", is(equalTo(30))));

            mockMvc.perform(get(API_PREFIX_V1 + "/dhus/search/count")
                    .param("q", "producttype:" + product.getName())
                    .param("rows", "10")
                    .param("orderby", "beginposition asc")
                    .with(jwtBearerToken(appUser, objectMapper)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", is(equalTo(30))));

            mockMvc.perform(get(API_PREFIX_V1 + "/dhus/search/count")
                    .param("q", "producttype:" + product.getName())
                    .param("orderby", "beginposition asc")
                    .with(jwtBearerToken(appUser, objectMapper)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", is(equalTo(30))));

            mockMvc.perform(get(API_PREFIX_V1 + "/dhus/search/count")
                    .param("q", "producttype:" + product.getName())
                    .param("start", "20")
                    .param("orderby", "beginposition asc")
                    .with(jwtBearerToken(appUser, objectMapper)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", is(equalTo(30))));
        }

        @Test
        public void shouldReturnScenesDhusForMultipleQueryParams() throws Exception {
            mockMvc.perform(get(API_PREFIX_V1 + "/dhus/search/count")
                    .param("q", "platformname:Sentinel-1A AND sensoroperationalmode:IW " +
                            "AND producttype:" + product.getName())
                    .with(jwtBearerToken(appUser, objectMapper)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", is(equalTo(30))));
            mockMvc.perform(get(API_PREFIX_V1 + "/dhus/search/count")
                    //yyyy-MM-ddThh:mm:ss.SSSZ
                    .param("q", "sensoroperationalmode:IW AND beginposition:[2019-11-01T00:00:00.000Z TO 2019-11-12T00:00:00.000Z] " +
                            "AND producttype:" + product.getName())
                    .with(jwtBearerToken(appUser, objectMapper)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", is(equalTo(30))));
            mockMvc.perform(get(API_PREFIX_V1 + "/dhus/search/count")
                    .param("q", "sensoroperationalmode:IW AND beginposition:[NOW-24MONTHS TO NOW] " +
                            "AND producttype:" + product.getName())
                    .with(jwtBearerToken(appUser, objectMapper)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", is(equalTo(30))));
        }

        @Test
        public void shouldReturnScenesDhusQueryPlatforname() throws Exception {
            //TODO: specific Sentinel not 1 or 2
            mockMvc.perform(get(API_PREFIX_V1 + "/dhus/search/count")
                    .param("q", "platformname:Sentinel-1A " +
                            "AND producttype:" + product.getName())
                    .with(jwtBearerToken(appUser, objectMapper)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", is(equalTo(30))));

            mockMvc.perform(get(API_PREFIX_V1 + "/dhus/search/count")
                    .param("q", "platformname:Sentinel-1 " +
                            "AND producttype:" + product.getName())
                    .with(jwtBearerToken(appUser, objectMapper)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", is(equalTo(0))));
        }

        @Test
        public void shouldReturnScenesDhusQueryByTime() throws Exception {
            mockMvc.perform(get(API_PREFIX_V1 + "/dhus/search/count")
                    .param("q", "beginposition:[2019-11-09T00:00:00.000000+00:00 TO 2019-11-12T00:00:00.000000+00:00] " +
                            "AND producttype:" + product.getName())
                    .with(jwtBearerToken(appUser, objectMapper)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", is(equalTo(3))));

            mockMvc.perform(get(API_PREFIX_V1 + "/dhus/search/count")
                    .param("q", "endposition:[2019-11-09T00:00:00.000000+00:00 TO 2019-11-12T00:00:00.000000+00:00] " +
                            "AND producttype:" + product.getName())
                    .with(jwtBearerToken(appUser, objectMapper)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", is(equalTo(3))));

            mockMvc.perform(get(API_PREFIX_V1 + "/dhus/search/count")
                    .param("q", "ingestiondate:[2019-11-09T00:00:00.000Z TO 2019-11-12T00:00:00.000Z] " +
                            "AND producttype:" + product.getName())
                    .with(jwtBearerToken(appUser, objectMapper)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", is(equalTo(3))));

            mockMvc.perform(get(API_PREFIX_V1 + "/dhus/search/count")
                    .param("q", "endposition:[NOW-24MONTHS TO NOW] " +
                            "AND producttype:" + product.getName())
                    .with(jwtBearerToken(appUser, objectMapper)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", is(equalTo(30))));

            mockMvc.perform(get(API_PREFIX_V1 + "/dhus/search/count")
                    .param("q", "endposition:[NOW-1DAY TO NOW-1HOUR] " +
                            "AND producttype:" + product.getName())
                    .with(jwtBearerToken(appUser, objectMapper)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", is(equalTo(0))));

            mockMvc.perform(get(API_PREFIX_V1 + "/dhus/search/count")
                    .param("q", "endposition:[NOW-5HOURS TO NOW-20MINUTES] " +
                            "AND producttype:" + product.getName())
                    .with(jwtBearerToken(appUser, objectMapper)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", is(equalTo(0))));
        }

        @Test
        public void shouldReturnScenesDhusQueryCollection() throws Exception {
            mockMvc.perform(get(API_PREFIX_V1 + "/dhus/search/count")
                    .param("q", "collection:S1B_24AU " +
                            "AND producttype:" + product.getName())
                    .with(jwtBearerToken(appUser, objectMapper)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", is(equalTo(30))));

            mockMvc.perform(get(API_PREFIX_V1 + "/dhus/search/count")
                    .param("q", "collection:not-exist " +
                            "AND producttype:" + product.getName())
                    .with(jwtBearerToken(appUser, objectMapper)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", is(equalTo(0))));
        }

        @Test
        public void shouldReturnScenesDhusQueryFootprintPolygon() throws Exception {
            mockMvc.perform(get(API_PREFIX_V1 + "/dhus/search/count")
                    .param("q", "footprint:Intersects(POLYGON((155.90 19.60,156.60 15.70,157.40 16.10,157.34 20.05,155.90 19.60))) " +
                            "AND producttype:" + product.getName())
                    .with(jwtBearerToken(appUser, objectMapper)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", is(equalTo(0))));

            mockMvc.perform(get(API_PREFIX_V1 + "/dhus/search/count")
                    .param("q", "footprint:Intersects(POLYGON((12.6 55,19 55.25,26 55,24.93 47.55,19 47.67,14 47.5,12.6 55))) " +
                            "AND producttype:" + product.getName())
                    .with(jwtBearerToken(appUser, objectMapper)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", is(equalTo(30))));
        }

        @Test
        public void shouldReturnScenesDhusQueryFootprintPoint() throws Exception {
            mockMvc.perform(get(API_PREFIX_V1 + "/dhus/search/count")
                    .param("q", "footprint:Intersects(15 51) " +
                            "AND producttype:" + product.getName())
                    .with(jwtBearerToken(appUser, objectMapper)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", is(equalTo(30))));

            mockMvc.perform(get(API_PREFIX_V1 + "/dhus/search/count")
                    .param("q", "footprint:Intersects(12 55) " +
                            "AND producttype:" + product.getName())
                    .with(jwtBearerToken(appUser, objectMapper)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", is(equalTo(0))));
        }

        @Test
        public void shouldReturnScenesDhusQueryOrbitNumber() throws Exception {
            // TODO: key:[from TO to] e.g. key:[0 TO 50]
            mockMvc.perform(get(API_PREFIX_V1 + "/dhus/search/count")
                    .param("q", "orbitnumber:05 " +
                            "AND producttype:" + product.getName())
                    .with(jwtBearerToken(appUser, objectMapper)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", is(equalTo(1))));

            mockMvc.perform(get(API_PREFIX_V1 + "/dhus/search/count")
                    .param("q", "orbitnumber:5 " +
                            "AND producttype:" + product.getName())
                    .with(jwtBearerToken(appUser, objectMapper)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", is(equalTo(0))));

            mockMvc.perform(get(API_PREFIX_V1 + "/dhus/search/count")
                    .param("q", "orbitnumber:not-exist " +
                            "AND producttype:" + product.getName())
                    .with(jwtBearerToken(appUser, objectMapper)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", is(equalTo(0))));
        }


        @Test
        public void shouldReturnScenesDhusQueryLastOrbitNumber() throws Exception {
            // TODO key:[from TO to] e.g. key:[0 TO 50]
            mockMvc.perform(get(API_PREFIX_V1 + "/dhus/search/count")
                    .param("q", "lastorbitnumber:05 " +
                            "AND producttype:" + product.getName())
                    .with(jwtBearerToken(appUser, objectMapper)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", is(equalTo(1))));

            mockMvc.perform(get(API_PREFIX_V1 + "/dhus/search/count")
                    .param("q", "lastorbitnumber:5 " +
                            "AND producttype:" + product.getName())
                    .with(jwtBearerToken(appUser, objectMapper)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", is(equalTo(0))));

            mockMvc.perform(get(API_PREFIX_V1 + "/dhus/search/count")
                    .param("q", "lastorbitnumber:not-exist " +
                            "AND producttype:" + product.getName())
                    .with(jwtBearerToken(appUser, objectMapper)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", is(equalTo(0))));
        }

        @Test
        public void shouldReturnScenesDhusQueryRelativeOrbitNumber() throws Exception {
            // TODO: key:[from TO to] e.g. key:[0 TO 50]
            mockMvc.perform(get(API_PREFIX_V1 + "/dhus/search/count")
                    .param("q", "relativeorbitnumber:5 " +
                            "AND producttype:" + product.getName())
                    .with(jwtBearerToken(appUser, objectMapper)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", is(equalTo(1))));

            mockMvc.perform(get(API_PREFIX_V1 + "/dhus/search/count")
                    .param("q", "relativeorbitnumber:05 " +
                            "AND producttype:" + product.getName())
                    .with(jwtBearerToken(appUser, objectMapper)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", is(equalTo(0))));

            mockMvc.perform(get(API_PREFIX_V1 + "/dhus/search/count")
                    .param("q", "relativeorbitnumber:not-exist " +
                            "AND producttype:" + product.getName())
                    .with(jwtBearerToken(appUser, objectMapper)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", is(equalTo(0))));
        }

        @Test
        public void shouldReturnScenesDhusQueryLastRelativeOrbitNumber() throws Exception {
            // TODO: key:[from TO to] e.g. key:[0 TO 50]
            mockMvc.perform(get(API_PREFIX_V1 + "/dhus/search/count")
                    .param("q", "lastrelativeorbitnumber:05 " +
                            "AND producttype:" + product.getName())
                    .with(jwtBearerToken(appUser, objectMapper)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", is(equalTo(0))));

            mockMvc.perform(get(API_PREFIX_V1 + "/dhus/search/count")
                    .param("q", "lastrelativeorbitnumber:5 " +
                            "AND producttype:" + product.getName())
                    .with(jwtBearerToken(appUser, objectMapper)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", is(equalTo(1))));

            mockMvc.perform(get(API_PREFIX_V1 + "/dhus/search/count")
                    .param("q", "lastrelativeorbitnumber:not-exist " +
                            "AND producttype:" + product.getName())
                    .with(jwtBearerToken(appUser, objectMapper)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", is(equalTo(0))));
        }

        @Test
        public void shouldReturnScenesDhusQueryPolarisation() throws Exception {
            mockMvc.perform(get(API_PREFIX_V1 + "/dhus/search/count")
                    .param("q", "polarisationmode:VV  VH " +
                            "AND producttype:" + product.getName())
                    .with(jwtBearerToken(appUser, objectMapper)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", is(equalTo(30))));

            mockMvc.perform(get(API_PREFIX_V1 + "/dhus/search/count")
                    .param("q", "polarisationmode:VV +  +  VH " +
                            "AND producttype:" + product.getName())
                    .with(jwtBearerToken(appUser, objectMapper)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", is(equalTo(30))));

            mockMvc.perform(get(API_PREFIX_V1 + "/dhus/search/count")
                    .param("q", "polarisationmode:VV " +
                            "AND producttype:" + product.getName())
                    .with(jwtBearerToken(appUser, objectMapper)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", is(equalTo(0))));

            mockMvc.perform(get(API_PREFIX_V1 + "/dhus/search/count")
                    .param("q", "polarisationmode:not-exist " +
                            "AND producttype:" + product.getName())
                    .with(jwtBearerToken(appUser, objectMapper)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", is(equalTo(0))));
        }

        @Test
        public void shouldReturnScenesDhusQueryProductType() throws Exception {
            mockMvc.perform(get(API_PREFIX_V1 + "/dhus/search/count")
                    .param("q", "producttype:" + product.getName())
                    .with(jwtBearerToken(appUser, objectMapper)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", is(equalTo(30))));

            mockMvc.perform(get(API_PREFIX_V1 + "/dhus/search/count")
                    .param("q", "producttype:not-exist")
                    .with(jwtBearerToken(appUser, objectMapper)))
                    .andExpect(status().isNotFound());
        }

        @Test
        public void shouldReturnScenesDhusQuerySensorOperationalMode() throws Exception {
            mockMvc.perform(get(API_PREFIX_V1 + "/dhus/search/count")
                    .param("q", "sensoroperationalmode:IW " +
                            "AND producttype:" + product.getName())
                    .with(jwtBearerToken(appUser, objectMapper)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", is(equalTo(30))));

            mockMvc.perform(get(API_PREFIX_V1 + "/dhus/search/count")
                    .param("q", "sensoroperationalmode:not-exist " +
                            "AND producttype:" + product.getName())
                    .with(jwtBearerToken(appUser, objectMapper)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", is(equalTo(0))));
        }

        @Test
        public void shouldReturnScenesDhusQueryCloudCoverage() throws Exception {
            // TODO: key:[80 TO 90]
            mockMvc.perform(get(API_PREFIX_V1 + "/dhus/search/count")
                    .param("q", "cloudcoveragepercentage:15 " +
                            "AND producttype:" + product.getName())
                    .with(jwtBearerToken(appUser, objectMapper)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", is(equalTo(16))));

            mockMvc.perform(get(API_PREFIX_V1 + "/dhus/search/count")
                    .param("q", "cloudcoveragepercentage:0 " +
                            "AND producttype:" + product.getName())
                    .with(jwtBearerToken(appUser, objectMapper)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", is(equalTo(1))));
        }

        @Test
        public void shouldReturnScenesDhusQueryTimeliness() throws Exception {
            mockMvc.perform(get(API_PREFIX_V1 + "/dhus/search/count")
                    .param("q", "timeliness:Near Real Time " +
                            "AND producttype:" + product.getName())
                    .with(jwtBearerToken(appUser, objectMapper)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", is(equalTo(30))));

            mockMvc.perform(get(API_PREFIX_V1 + "/dhus/search/count")
                    .param("q", "timeliness:Near Real Time2 " +
                            "AND producttype:" + product.getName())
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
