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

package pl.cyfronet.s4e.controller;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
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

import java.time.Clock;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static pl.cyfronet.s4e.Constants.API_PREFIX_V1;
import static pl.cyfronet.s4e.SceneTestHelper.productBuilder;
import static pl.cyfronet.s4e.SceneTestHelper.toScene;
import static pl.cyfronet.s4e.search.SearchQueryParams.*;

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
    private Clock clock;
    @Autowired
    private MockMvc mockMvc;
    Product product;

    @BeforeEach
    public void setUp() {
        testDbHelper.clean();
        //add product
        product = productRepository.save(productBuilder()
                .granuleArtifactRule(Map.of("default", "quicklook"))
                .build());
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

    private Scene buildScene(Product product, long number) {
        JsonNode metadataContent = SceneTestHelper.getMetadataContentWithNumber(number);
        Scene scene = SceneTestHelper.sceneWithMetadataBuilder(product, metadataContent).build();
        scene.setSceneContent(SceneTestHelper.getSceneContent());
        return scene;
    }

    private static Stream<Arguments> shouldReturnErrorsForParam() {
        return Stream.of(
                Arguments.of(LIMIT, "abc", "Limit musi być liczbą dodatnią"),
                Arguments.of(LIMIT, "-1", "Limit musi być liczbą dodatnią"),
                Arguments.of(OFFSET, "abc", "Offset musi być liczbą dodatnią"),
                Arguments.of(OFFSET, "-1", "Offset musi być liczbą dodatnią"),
                Arguments.of(CLOUD_COVER, "abc", "Cloud cover musi być liczbą z zakresu [0 - 100]"),
                Arguments.of(CLOUD_COVER, "-1", "Cloud cover musi być liczbą z zakresu [0 - 100]"),
                Arguments.of(CLOUD_COVER, "1000", "Cloud cover musi być liczbą z zakresu [0 - 100]"),
                Arguments.of(INGESTION_FROM, "2019-11-08", "Zły format daty: `2019-11-08`"),
                Arguments.of(INGESTION_FROM, "Hakuna matata", "Zły format daty: `Hakuna matata`")
        );
    }

    @ParameterizedTest
    @MethodSource
    public void shouldReturnErrorsForParam(String param, String value, String text) throws Exception {
        mockMvc.perform(get(API_PREFIX_V1 + "/search")
                .param(param, value)
                .param("productType", product.getName()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$." + param, is(Arrays.asList(text))));
    }

    @Nested
    class Search {
        @Test
        public void shouldGetSceneBySearchEndpoint() throws Exception {
            // default limit is 20
            int limit = 20;
            mockMvc.perform(get(API_PREFIX_V1 + "/search")
                    .param("timeZone", "UTC")
                    .param("productType", product.getName())
                    .param("limit", String.valueOf(limit)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()", is(equalTo(limit))))
                    .andExpect(jsonPath("$[0].id").isNotEmpty())
                    .andExpect(jsonPath("$[0].productId").isNotEmpty())
                    .andExpect(jsonPath("$[0].sceneKey", startsWith("path/to/")))
                    .andExpect(jsonPath("$[0].artifacts", containsInAnyOrder(
                            "RGB_16b", "RGBs_8b", "checksum", "manifest", "metadata", "quicklook", "product_archive"
                    )))
                    .andExpect(jsonPath("$[0].timestamp").value("2019-11-03T05:07:42.047432Z"));
        }

        @Test
        public void shouldntReturnErrorByTooLargeLimit() throws Exception {
            mockMvc.perform(get(API_PREFIX_V1 + "/search")
                    .param("productType", product.getName())
                    .param("limit", "1000"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()", is(equalTo(30))));
        }

        @Test
        public void shouldGetSceneBySensingTime() throws Exception {
            mockMvc.perform(get(API_PREFIX_V1 + "/search")
                    .param("timeZone", "UTC")
                    .param("productType", product.getName())
                    .param("sensingFrom", "2019-11-07T00:00:00.000000-07:00")
                    .param("sensingTo", "2019-11-12T00:00:00.000000+00:00"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()", is(equalTo(6))));
        }

        @Test
        public void shouldntGetSceneBySensingTimeWrongFormat() throws Exception {
            mockMvc.perform(get(API_PREFIX_V1 + "/search")
                    .param("timeZone", "UTC")
                    .param("productType", product.getName())
                    .param("sensingFrom", "2019-11-08")
                    .param("sensingTo", "2019-11-12"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.sensingTo", is(Arrays.asList("Zły format daty: `2019-11-12`"))))
                    .andExpect(jsonPath("$.sensingFrom", is(Arrays.asList("Zły format daty: `2019-11-08`"))));
        }

        @Test
        public void shouldGetSceneByIngestionTime() throws Exception {
            mockMvc.perform(get(API_PREFIX_V1 + "/search?timeZone=UTC&collection=S1B_24AU")
                    .param("timeZone", "UTC")
                    .param("productType", product.getName())
                    .param("ingestionFrom", "2019-11-08T00:00:00.000000-07:00")
                    .param("ingestionTo", "2019-11-12T00:00:00.000000+00:00"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()", is(equalTo(3))));
        }

        @Test
        public void shouldntGetSceneByIngestionTimeWrongFormat() throws Exception {
            mockMvc.perform(get(API_PREFIX_V1 + "/search")
                    .param("timeZone", "UTC")
                    .param("productType", product.getName())
                    .param("ingestionFrom", "2019-11-08")
                    .param("ingestionTo", "2019-11-12"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.ingestionTo", is(Arrays.asList("Zły format daty: `2019-11-12`"))))
                    .andExpect(jsonPath("$.ingestionFrom", is(Arrays.asList("Zły format daty: `2019-11-08`"))));
        }

        @Test
        public void shouldGetSceneBySatellitePlatform() throws Exception {
            mockMvc.perform(get(API_PREFIX_V1 + "/search")
                    .param("satellitePlatform", "Sentinel-1A")
                    .param("productType", product.getName())
                    .param("limit", "30"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()", is(equalTo(30))))
                    .andReturn();
        }

        @Test
        public void shouldGetSceneByProductType() throws Exception {
            mockMvc.perform(get(API_PREFIX_V1 + "/search")
                    .param("productType", product.getName())
                    .param("productType", product.getName())
                    .param("limit", "30"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()", is(equalTo(30))))
                    .andReturn();
        }

        @Test
        public void shouldGetSceneByPolarisation() throws Exception {
            mockMvc.perform(get(API_PREFIX_V1 + "/search")
                    .param("polarisation", "VV VH")
                    .param("productType", product.getName())
                    .param("limit", "30"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()", is(equalTo(30))))
                    .andReturn();
        }

        @Test
        public void shouldGetSceneBySensorMode() throws Exception {
            mockMvc.perform(get(API_PREFIX_V1 + "/search")
                    .param("sensorMode", "IW")
                    .param("productType", product.getName())
                    .param("limit", "30"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()", is(equalTo(30))))
                    .andReturn();
        }

        @Test
        public void shouldGetSceneByCollection() throws Exception {
            mockMvc.perform(get(API_PREFIX_V1 + "/search")
                    .param("collection", "S1B_24AU")
                    .param("productType", product.getName())
                    .param("limit", "30"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()", is(equalTo(30))));
        }

        @Test
        public void shouldGetSceneByByCloudCover() throws Exception {
            mockMvc.perform(get(API_PREFIX_V1 + "/search")
                    .param("cloudCover", "0.4f")
                    .param("productType", product.getName())
                    .param("limit", "30"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()", is(equalTo(1))));
        }

        @Test
        public void shouldGetSceneByByTimeliness() throws Exception {
            mockMvc.perform(get(API_PREFIX_V1 + "/search")
                    .param("timeliness", "Near Real Time")
                    .param("productType", product.getName())
                    .param("limit", "30"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()", is(equalTo(30))));
        }

        @Test
        public void shouldGetSceneByProcessingLevel() throws Exception {
            mockMvc.perform(get(API_PREFIX_V1 + "/search")
                    .param("processingLevel", "2LC")
                    .param("productType", product.getName())
                    .param("limit", "30"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()", is(equalTo(3))));
        }

        @Test
        public void shouldGetSceneByProductLevel() throws Exception {
            mockMvc.perform(get(API_PREFIX_V1 + "/search")
                    .param("productLevel", "L2")
                    .param("productType", product.getName())
                    .param("limit", "30"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()", is(equalTo(3))));
        }

        @Test
        public void shouldGetSceneWithSortBySensingTime() throws Exception {
            mockMvc.perform(get(API_PREFIX_V1 + "/search")
                    .param("sortBy", "sensingTime")
                    .param("productType", product.getName())
                    .param("limit", "30"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()", is(equalTo(30))));
        }

        @Test
        public void shouldGetSceneWithSortByIngestionTime() throws Exception {
            mockMvc.perform(get(API_PREFIX_V1 + "/search")
                    .param("sortBy", "ingestionTime")
                    .param("productType", product.getName())
                    .param("limit", "30"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()", is(equalTo(30))));
        }

        @Test
        public void shouldReturnFilteredByEumetsatLicenseScenes() throws Exception {
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

            mockMvc.perform(get(API_PREFIX_V1 + "/search")
                    .param("sortBy", "sensingTime")
                    .param("productType", product.getName())
                    .param("limit", "30"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()", is(equalTo(3))))
                    .andExpect(jsonPath("$..id", contains(
                            equalTo(scenes.get(2).getId().intValue()),
                            equalTo(scenes.get(3).getId().intValue()),
                            equalTo(scenes.get(4).getId().intValue()))));
        }
    }

    @Nested
    class Count {
        @Test
        public void shouldGetSceneBySearchEndpoint() throws Exception {
            // default limit is 20
            int limit = 20;
            mockMvc.perform(get(API_PREFIX_V1 + "/search/count")
                    .param("timeZone", "UTC")
                    .param("productType", product.getName())
                    .param("limit", String.valueOf(limit)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", is(equalTo(30))));
        }

        @Test
        public void shouldntReturnErrorByTooLargeLimit() throws Exception {
            mockMvc.perform(get(API_PREFIX_V1 + "/search/count")
                    .param("productType", product.getName())
                    .param("limit", "1000"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", is(equalTo(30))));
        }

        @Test
        public void shouldGetSceneBySensingTime() throws Exception {
            mockMvc.perform(get(API_PREFIX_V1 + "/search/count")
                    .param("productType", product.getName())
                    .param("timeZone", "UTC")
                    .param("sensingFrom", "2019-11-07T00:00:00.000000-07:00")
                    .param("sensingTo", "2019-11-12T00:00:00.000000+00:00"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", is(equalTo(6))));
        }

        @Test
        public void shouldntGetSceneBySensingTimeWrongFormat() throws Exception {
            mockMvc.perform(get(API_PREFIX_V1 + "/search/count")
                    .param("timeZone", "UTC")
                    .param("productType", product.getName())
                    .param("sensingFrom", "2019-11-08")
                    .param("sensingTo", "2019-11-12"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.sensingTo", is(Arrays.asList("Zły format daty: `2019-11-12`"))))
                    .andExpect(jsonPath("$.sensingFrom", is(Arrays.asList("Zły format daty: `2019-11-08`"))));
        }

        @Test
        public void shouldGetSceneByIngestionTime() throws Exception {
            mockMvc.perform(get(API_PREFIX_V1 + "/search/count")
                    .param("timeZone", "UTC")
                    .param("productType", product.getName())
                    .param("ingestionFrom", "2019-11-08T00:00:00.000000-07:00")
                    .param("ingestionTo", "2019-11-12T00:00:00.000000+00:00"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", is(equalTo(3))));
        }

        @Test
        public void shouldntGetSceneByIngestionTimeWrongFormat() throws Exception {
            mockMvc.perform(get(API_PREFIX_V1 + "/search/count")
                    .param("timeZone", "UTC")
                    .param("productType", product.getName())
                    .param("ingestionFrom", "2019-11-08")
                    .param("ingestionTo", "2019-11-12"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.ingestionTo", is(Arrays.asList("Zły format daty: `2019-11-12`"))))
                    .andExpect(jsonPath("$.ingestionFrom", is(Arrays.asList("Zły format daty: `2019-11-08`"))));
        }

        @Test
        public void shouldGetSceneBySatellitePlatform() throws Exception {
            mockMvc.perform(get(API_PREFIX_V1 + "/search/count")
                    .param("productType", product.getName())
                    .param("satellitePlatform", "Sentinel-1A")
                    .param("limit", "30"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", is(equalTo(30))))
                    .andReturn();
        }

        @Test
        public void shouldGetSceneByProductType() throws Exception {
            mockMvc.perform(get(API_PREFIX_V1 + "/search/count")
                    .param("productType", product.getName())
                    .param("limit", "30"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", is(equalTo(30))))
                    .andReturn();
        }

        @Test
        public void shouldGetSceneByPolarisation() throws Exception {
            mockMvc.perform(get(API_PREFIX_V1 + "/search/count")
                    .param("polarisation", "VV VH")
                    .param("productType", product.getName())
                    .param("limit", "30"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", is(equalTo(30))))
                    .andReturn();
        }

        @Test
        public void shouldGetSceneBySensorMode() throws Exception {
            mockMvc.perform(get(API_PREFIX_V1 + "/search/count")
                    .param("sensorMode", "IW")
                    .param("productType", product.getName())
                    .param("limit", "30"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", is(equalTo(30))))
                    .andReturn();
        }

        @Test
        public void shouldGetSceneByCollection() throws Exception {
            mockMvc.perform(get(API_PREFIX_V1 + "/search/count")
                    .param("collection", "S1B_24AU")
                    .param("productType", product.getName())
                    .param("limit", "30"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", is(equalTo(30))));
        }

        @Test
        public void shouldGetSceneByByCloudCover() throws Exception {
            mockMvc.perform(get(API_PREFIX_V1 + "/search/count")
                    .param("cloudCover", "0.4f")
                    .param("productType", product.getName())
                    .param("limit", "30"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", is(equalTo(1))));
        }

        @Test
        public void shouldGetSceneByByTimeliness() throws Exception {
            mockMvc.perform(get(API_PREFIX_V1 + "/search/count")
                    .param("timeliness", "Near Real Time")
                    .param("productType", product.getName())
                    .param("limit", "30"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", is(equalTo(30))));
        }

        @Test
        public void shouldGetSceneByProcessingLevel() throws Exception {
            mockMvc.perform(get(API_PREFIX_V1 + "/search/count")
                    .param("processingLevel", "2LC")
                    .param("productType", product.getName())
                    .param("limit", "30"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", is(equalTo(3))));
        }

        @Test
        public void shouldGetSceneByProductLevel() throws Exception {
            mockMvc.perform(get(API_PREFIX_V1 + "/search/count")
                    .param("productLevel", "L2")
                    .param("productType", product.getName())
                    .param("limit", "30"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", is(equalTo(3))));
        }

        @Test
        public void shouldGetSceneWithSortBySensingTime() throws Exception {
            mockMvc.perform(get(API_PREFIX_V1 + "/search/count")
                    .param("sortBy", "sensingTime")
                    .param("productType", product.getName())
                    .param("limit", "30"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", is(equalTo(30))));
        }

        @Test
        public void shouldGetSceneWithSortByIngestionTime() throws Exception {
            mockMvc.perform(get(API_PREFIX_V1 + "/search/count")
                    .param("sortBy", "ingestionTime")
                    .param("productType", product.getName())
                    .param("limit", "30"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", is(equalTo(30))));
        }

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

            mockMvc.perform(get(API_PREFIX_V1 + "/search/count")
                    .param("sortBy", "ingestionTime")
                    .param("productType", product.getName())
                    .param("limit", "30"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", is(equalTo(3))));
        }
    }

}
