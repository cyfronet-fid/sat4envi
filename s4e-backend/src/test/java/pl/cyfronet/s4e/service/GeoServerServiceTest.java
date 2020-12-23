/*
 * Copyright 2020 ACC Cyfronet AGH
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import pl.cyfronet.s4e.BasicTest;
import pl.cyfronet.s4e.Constants;
import pl.cyfronet.s4e.OverlayHelper;
import pl.cyfronet.s4e.TestDbHelper;
import pl.cyfronet.s4e.bean.*;
import pl.cyfronet.s4e.data.repository.*;
import pl.cyfronet.s4e.geoserver.op.GeoServerOperations;
import pl.cyfronet.s4e.properties.GeoServerProperties;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
import static pl.cyfronet.s4e.SceneTestHelper.productBuilder;
import static pl.cyfronet.s4e.SceneTestHelper.sceneBuilder;

@BasicTest
class GeoServerServiceTest {

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
    private GeoServerProperties geoServerProperties;

    @Autowired
    private TestDbHelper testDbHelper;

    @Mock
    private GeoServerOperations geoServerOperations;

    private GeoServerService geoServerService;
    private Product product;
    private SldStyle sldStyle;
    private PRGOverlay prgOverlay;

    @BeforeEach
    public void beforeEach() {
        testDbHelper.clean();
        prepare();
    }

    private void prepare() {
        geoServerService = new GeoServerService(geoServerOperations, geoServerProperties);

        product = productRepository.save(productBuilder().build());
        sceneRepository.save(sceneBuilder(product).build());
        sldStyle = sldStyleRepository.save(sldStyleBuilder().build());

        val wmsOverlay = wmsOverlayRepository.save(
                OverlayHelper.wmsOverlayBuilder()
                        .ownerType(OverlayOwner.GLOBAL)
                        .url("")
                        .build()
        );
        prgOverlay = prgOverlayRepository.save(prgOverlayBuilder(sldStyle, wmsOverlay).build());
    }

    private SldStyle.SldStyleBuilder sldStyleBuilder() {
        return SldStyle.builder()
                .name("styleOne");
    }

    private PRGOverlay.PRGOverlayBuilder prgOverlayBuilder(SldStyle sldStyle, WMSOverlay wmsOverlay) {
        return PRGOverlay.builder()
                .wmsOverlay(wmsOverlay)
                .featureType("wojewodztwaFeatureType")
                .sldStyle(sldStyle);
    }

    @Test
    public void shouldAddStyleAndSetCreatedFlag() {
        geoServerService.addStyle(sldStyle);

        verify(geoServerOperations, times(1)).createStyle(geoServerProperties.getWorkspace(), sldStyle.getName());
        verify(geoServerOperations, times(1)).uploadSld(geoServerProperties.getWorkspace(), sldStyle.getName(), sldStyle.getName());
        verifyNoMoreInteractions(geoServerOperations);
    }

    @Test
    public void shouldCreatePrgLayers() {
        sldStyle.setCreated(true);
        sldStyle = sldStyleRepository.save(sldStyle);
        when(geoServerOperations.layerExists(geoServerProperties.getWorkspace(), prgOverlay.getFeatureType())).thenReturn(true);

        List<PRGOverlay> prgOverlays = new ArrayList<>();
        prgOverlayRepository.findAll().forEach(prgOverlays::add);

        geoServerService.createPrgOverlays(prgOverlays);

        verify(geoServerOperations).createExternalShpDataStore(geoServerProperties.getWorkspace(), Constants.GEOSERVER_PRG_DATA_STORE, "file://"+Constants.GEOSERVER_PRG_PATH);
        verify(geoServerOperations).setLayerDefaultStyle(geoServerProperties.getWorkspace(), prgOverlay.getFeatureType(), sldStyle.getName());
        verify(geoServerOperations).createTileLayer(geoServerProperties.getWorkspace(), prgOverlay.getFeatureType());
        verifyNoMoreInteractions(geoServerOperations);
    }

    @Test
    public void shouldNotCreatePrgLayersIfAnyCreated() {
        prgOverlay.setCreated(true);
        prgOverlay = prgOverlayRepository.save(prgOverlay);

        List<PRGOverlay> prgOverlays = new ArrayList<>();
        prgOverlayRepository.findAll().forEach(prgOverlays::add);

        geoServerService.createPrgOverlays(prgOverlays);

        verifyNoMoreInteractions(geoServerOperations);
    }

    @Test
    public void shouldThrowIfSldStyleNotCreatedForPrgOverlay() {
        List<PRGOverlay> prgOverlays = new ArrayList<>();
        prgOverlayRepository.findAll().forEach(prgOverlays::add);

        assertThrows(IllegalStateException.class, () -> geoServerService.createPrgOverlays(prgOverlays));

        verifyNoMoreInteractions(geoServerOperations);
    }
}
