/*
 * Copyright 2022 ACC Cyfronet AGH
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

import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;
import pl.cyfronet.s4e.IntegrationTest;
import pl.cyfronet.s4e.TestDbHelper;
import pl.cyfronet.s4e.bean.*;
import pl.cyfronet.s4e.data.repository.*;
import pl.cyfronet.s4e.geoserver.op.GeoServerOperations;
import pl.cyfronet.s4e.geoserver.op.SeedProductsTest;
import pl.cyfronet.s4e.properties.GeoServerProperties;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@IntegrationTest
public class GeoServerServiceIntegrationTest {
    @Autowired
    private GeoServerProperties geoServerProperties;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private SceneRepository sceneRepository;

    @Autowired
    private SldStyleRepository sldStyleRepository;

    @Autowired
    private PRGOverlayRepository prgOverlayRepository;

    @Autowired
    private WMSOverlayRepository wmsOverlayRepository;

    @Autowired
    private GeoServerOperations geoServerOperations;

    @Autowired
    private GeoServerService geoServerService;

    @Autowired
    private SeedProductsTest seedProductsTest;

    @Autowired
    private TestDbHelper testDbHelper;

    @BeforeEach
    public void beforeEach() {
        testDbHelper.clean();
        geoServerService.resetWorkspace();
    }

    @Test
    public void shouldCreateSldStyle() {
        SldStyle sldStyle = sldStyleRepository.save(SldStyle.builder()
                .name("styleOne")
                .build());

        geoServerService.addStyle(sldStyle);

        assertThat(geoServerOperations.listStyles(geoServerProperties.getWorkspace()), contains("styleOne"));
    }

    @Transactional
    @Test
    public void shouldCreatePrgLayers() {
        SldStyle sldStyle = sldStyleRepository.save(SldStyle.builder()
                .name("wojewodztwa")
                .build());
        geoServerService.addStyle(sldStyle);
        sldStyle.setCreated(true);
        val wmsOverlay = wmsOverlayRepository.save(
                WMSOverlay.builder()
                        .label("wojewodztwa")
                        .url("")
                        .layerName("wojewodztwa")
                        .ownerType(OverlayOwner.GLOBAL)
                        .build()
        );
        PRGOverlay prgOverlay = prgOverlayRepository.save(PRGOverlay.builder()
                .featureType("wojewodztwa")
                .sldStyle(sldStyle)
                .wmsOverlay(wmsOverlay)
                .build());

        List<PRGOverlay> prgOverlays = prgOverlayRepository.findAll();

        geoServerService.createPrgOverlays(prgOverlays);

        assertThat(geoServerOperations.layerExists("test", "wojewodztwa"), is(true));
        assertThat(geoServerOperations.getLayer("test", "wojewodztwa").getLayer().getDefaultStyle().getName(), is(equalTo("test:wojewodztwa")));
        assertThat(geoServerOperations.tileLayerExists("test", "wojewodztwa"), is(true));
    }

    @Test
    public void shouldAddStoreAndLayer() {
        seedProductsTest.prepareDb();
        Product product = productRepository.findByName("108m").get();

        assertThat(geoServerOperations.layerExists("test", "108m"), is(false));
        assertThat(geoServerOperations.tileLayerExists("test", "108m"), is(false));

        geoServerService.addStoreAndLayer(product);

        assertThat(geoServerOperations.layerExists("test", "108m"), is(true));
        assertThat(geoServerOperations.getLayer("test", "108m").getLayer().getName(),
                is(equalTo("108m")));
        assertThat(geoServerOperations.tileLayerExists("test", "108m"), is(true));
    }

    @Test
    public void shouldntAddStoreAndLayer() {
        Product product = Product.builder()
                .name("test")
                .layerName("test")
                .displayName("test")
                .description("Description")
                .rank(1000L)
                .build();
        Assertions.assertThrows(RestClientException.class, () -> geoServerService.addStoreAndLayer(product));
    }

    @Test
    public void shouldCheckIfLayerExists() {
        seedProductsTest.prepareDb();
        Product product = productRepository.findByName("108m").get();
        geoServerService.addStoreAndLayer(product);

        assertThat(geoServerOperations.layerExists("test", "108m"), is(true));
        assertThat(geoServerOperations.layerExists("test", "108m12"), is(false));
    }
}
