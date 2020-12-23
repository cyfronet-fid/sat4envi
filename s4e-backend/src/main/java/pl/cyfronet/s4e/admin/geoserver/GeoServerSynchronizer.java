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

package pl.cyfronet.s4e.admin.geoserver;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.cyfronet.s4e.bean.PRGOverlay;
import pl.cyfronet.s4e.data.repository.PRGOverlayRepository;
import pl.cyfronet.s4e.data.repository.ProductRepository;
import pl.cyfronet.s4e.data.repository.SldStyleRepository;
import pl.cyfronet.s4e.service.GeoServerService;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class GeoServerSynchronizer {
    private final SldStyleRepository sldStyleRepository;
    private final ProductRepository productRepository;
    private final PRGOverlayRepository prgOverlayRepository;

    private final GeoServerService geoServerService;

    @Transactional
    public void synchronizeStoreAndMosaics() {
        log.info("Creating stores and mosaics");
        for (val product : productRepository.findAll()) {
            if (!geoServerService.layerExists(product.getLayerName())) {
                geoServerService.addStoreAndLayer(product);
            }
        }
    }

    @Transactional
    public void synchronizeOverlays() {
        log.info("Creating styles");
        for (val sldStyle : sldStyleRepository.findAll()) {
            if (!sldStyle.isCreated()) {
                geoServerService.addStyle(sldStyle);
                sldStyle.setCreated(true);
            }
        }

        log.info("Creating PRG overlays");
        List<PRGOverlay> prgOverlays = new ArrayList<>();
        prgOverlayRepository.findAll().forEach(prgOverlays::add);
        // existence check is done inside
        geoServerService.createPrgOverlays(prgOverlays);
        for (val prgOverlay : prgOverlays) {
            if (geoServerService.layerExists(prgOverlay.getFeatureType())) {
                prgOverlay.setCreated(true);
            }
        }
    }
}
