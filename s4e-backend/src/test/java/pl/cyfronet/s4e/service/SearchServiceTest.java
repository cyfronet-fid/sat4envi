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

package pl.cyfronet.s4e.service;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import pl.cyfronet.s4e.BasicTest;
import pl.cyfronet.s4e.SceneTestHelper;
import pl.cyfronet.s4e.TestDbHelper;
import pl.cyfronet.s4e.api.MappedScene;
import pl.cyfronet.s4e.bean.Product;
import pl.cyfronet.s4e.bean.Scene;
import pl.cyfronet.s4e.data.repository.ProductRepository;
import pl.cyfronet.s4e.data.repository.SceneRepository;
import pl.cyfronet.s4e.ex.NotFoundException;
import pl.cyfronet.s4e.search.SearchQueryParams;
import pl.cyfronet.s4e.security.AppUserDetails;

import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
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

    private Product product;

    @BeforeEach
    public void setUp() {
        testDbHelper.clean();
        //add product
        product = productRepository.save(productBuilder().accessType(Product.AccessType.EUMETSAT).build());
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

    private Scene buildScene(Product product, long number) {
        JsonNode metadataContent = SceneTestHelper.getMetadataContentWithNumber(number);
        Scene scene = SceneTestHelper.sceneWithMetadataBuilder(product, metadataContent).build();
        scene.setSceneContent(SceneTestHelper.getSceneContent());
        return scene;
    }

    @Test
    public void testQueryBySensingTime() throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("sensingFrom", "2019-11-05T00:00:00.000000+00:00");
        params.put("sensingTo", "2019-11-10T00:00:00.000000+00:00");
        List<MappedScene> scenes = searchService.getScenesBy(params);
        assertThat(scenes, hasSize(5));
    }

    @Test
    public void testQueryByIngestionTime() throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("ingestionFrom", "2019-11-09T00:00:00.000000+00:00");
        params.put("ingestionTo", "2019-11-12T00:00:00.000000+00:00");
        List<MappedScene> scenes = searchService.getScenesBy(params);
        assertThat(scenes, hasSize(1));
    }

    @Test
    public void testQueryBySatellitePlatform() throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("satellitePlatform", "Sentinel-1A");
        List<MappedScene> scenes = searchService.getScenesBy(params);
        assertThat(scenes, hasSize(10));
    }

    @Test
    public void testQueryByProductType() throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("productType", product.getName());
        List<MappedScene> scenes = searchService.getScenesBy(params);
        assertThat(scenes, hasSize(10));
    }

    @Test
    public void testQueryByPolarisation() throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("polarisation", "VV VH");
        List<MappedScene> scenes = searchService.getScenesBy(params);
        assertThat(scenes, hasSize(10));
    }

    @Test
    public void testQueryBySensorMode() throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("sensorMode", "IW");
        List<MappedScene> scenes = searchService.getScenesBy(params);
        assertThat(scenes, hasSize(10));
    }

    @Test
    public void testQueryByCollection() throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("collection", "S1B_24AU");
        List<MappedScene> scenes = searchService.getScenesBy(params);
        assertThat(scenes, hasSize(10));
    }

    @Test
    public void testQueryByCloudCover() throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("cloudCover", 0.4f);
        List<MappedScene> scenes = searchService.getScenesBy(params);
        assertThat(scenes, hasSize(1));
    }

    @Test
    public void testQueryByTimeliness() throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("timeliness", "Near Real Time");
        List<MappedScene> scenes = searchService.getScenesBy(params);
        assertThat(scenes, hasSize(10));
    }

    @Test
    public void testQueryByInstrument() throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("instrument", "OLCI");
        List<MappedScene> scenes = searchService.getScenesBy(params);
        assertThat(scenes, hasSize(10));
    }

    @Test
    public void testQueryByProductLevel() throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("productLevel", "L2");
        List<MappedScene> scenes = searchService.getScenesBy(params);
        assertThat(scenes, hasSize(1));
    }

    @Test
    public void testQueryByProcessingLevel() throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("processingLevel", "2LC");
        List<MappedScene> scenes = searchService.getScenesBy(params);
        assertThat(scenes, hasSize(1));
    }

    @Test
    public void testQueryByRelativeOrbitNumber() throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("relativeOrbitNumber", 9);
        List<MappedScene> scenes = searchService.getScenesBy(params);
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
        List<MappedScene> scenes = searchService.getScenesBy(params);
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
        params.put("cloudCover", 20);
        params.put("polarisation", "VV VH");
        params.put("productType", product.getName());
        params.put("satellitePlatform", "Sentinel-1A");
        params.put("sensingFrom", "2019-11-01T00:00:00.000000+00:00");
        params.put("sensingTo", "2019-11-12T00:00:00.000000+00:00");
        params.put("ingestionFrom", "2019-11-01T00:00:00.000000+00:00");
        params.put("ingestionTo", "2019-11-12T00:00:00.000000+00:00");
        params.put("order", "DESC");
        params.put("sortBy", "sensingTime");
        params.put("limit", 15);
        List<MappedScene> scenes = searchService.getScenesBy(params);
        assertThat(scenes, hasSize(1));
    }

    @Test
    public void testSqlInjection() throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("relativeOrbitNumber", 2);
        params.put("polarisation", "Dual VV VH\'UNION SELECT username, password FROM users--");
        params.put("order", "DESC");
        params.put("sortBy", "ingestionTime");
        params.put("limit", 15);
        params.put("offset", 1);
        List<MappedScene> scenes = searchService.getScenesBy(params);
        assertThat(scenes, hasSize(0));
    }

    @Test
    public void applyLicenseByProductTypeReturnForbidden() {
        val product = productRepository.save(productBuilder().accessType(Product.AccessType.PRIVATE).build());
        val userDetails = mock(AppUserDetails.class);
        when(userDetails.getAuthorities()).thenReturn(Set.of());
        Map<String, Object> params = new HashMap<>();
        params.put("productType", product.getName());
        assertThrows(AccessDeniedException.class, () -> searchService.applyLicenseByProductType(params, userDetails));
    }

    @Test
    public void applyLicenseByProductTypeReturnNotFound() {
        val userDetails = mock(AppUserDetails.class);
        Map<String, Object> params = new HashMap<>();
        params.put("productType", "name");

        assertThrows(NotFoundException.class, () -> searchService.applyLicenseByProductType(params, userDetails));
    }

    @Test
    public void applyLicenseByProductTypeAddAccessType() throws NotFoundException {
        val userDetails = mock(AppUserDetails.class);
        when(userDetails.getAuthorities()).thenReturn(Set.of());
        Map<String, Object> params = new HashMap<>();
        params.put("productType", product.getName());
        searchService.applyLicenseByProductType(params, userDetails);
        assertThat(params.entrySet(), hasSize(2));
        assertThat(params, hasEntry(SearchQueryParams.ACCESS_TYPE, Product.AccessType.EUMETSAT));
        assertThat(params, hasEntry("productType", product.getName()));
    }
}
