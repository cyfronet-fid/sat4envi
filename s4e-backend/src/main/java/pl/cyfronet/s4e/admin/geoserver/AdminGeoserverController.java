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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import pl.cyfronet.s4e.bean.OverlayOwner;
import pl.cyfronet.s4e.bean.PRGOverlay;
import pl.cyfronet.s4e.bean.SldStyle;
import pl.cyfronet.s4e.bean.WMSOverlay;
import pl.cyfronet.s4e.data.repository.PRGOverlayRepository;
import pl.cyfronet.s4e.data.repository.SldStyleRepository;
import pl.cyfronet.s4e.data.repository.WMSOverlayRepository;
import pl.cyfronet.s4e.properties.GeoServerProperties;
import pl.cyfronet.s4e.service.GeoServerService;

import java.util.stream.Stream;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static pl.cyfronet.s4e.Constants.ADMIN_PREFIX;

@RestController
@RequestMapping(path = ADMIN_PREFIX + "/geoserver", produces = APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "admin-geoserver", description = "The Admin GeoServer API")
@Slf4j
public class AdminGeoserverController {
    private final GeoServerProperties geoServerProperties;
    private final GeoServerService geoServerService;
    private final GeoServerSynchronizer geoServerSynchronizer;

    private final SldStyleRepository sldStyleRepository;
    private final PRGOverlayRepository prgOverlayRepository;
    private final WMSOverlayRepository wmsOverlayRepository;

    @Operation(summary = "Reset GeoServer workspace")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Success", content = @Content),
            @ApiResponse(responseCode = "400", description = "Error occurred", content = @Content)
    })
    @PostMapping(path = "/reset-workspace")
    public void resetWorkspace() {
        log.info("Resetting GeoServer workspace");
        geoServerService.resetWorkspace();
    }

    @Operation(summary = "Seed Overlays")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Success", content = @Content),
            @ApiResponse(responseCode = "400", description = "Error occurred", content = @Content)
    })
    @PostMapping(path = "/seed-overlays")
    public void seedOverlays(@RequestParam(defaultValue = "true") Boolean syncGeoserver) {
        log.info("Seeding overlays (syncGeoserver=" + syncGeoserver + ")");
        prgOverlayRepository.deleteAll();
        wmsOverlayRepository.deleteAll();
        sldStyleRepository.deleteAll();

        SldStyle sldStyle = sldStyleRepository.save(SldStyle.builder()
                .name("wojewodztwa")
                .build());

        Stream.of(
                "wojewodztwa",
                "powiaty",
                "gminy",
                "jednostki_ewidencyjne",
                "obreby_ewidencyjne"
        )
                .map(name -> WMSOverlay.builder()
                        .label(name)
                        .url("")
                        .layerName(geoServerProperties.getWorkspace() + ':' + name)
                        .ownerType(OverlayOwner.GLOBAL)
                        .build())
                .map(wmsOverlayRepository::save)
                .map(wmsOverlay -> PRGOverlay.builder()
                        .featureType(wmsOverlay.getLabel())
                        .wmsOverlay(wmsOverlay)
                        .created(!syncGeoserver)
                        .sldStyle(sldStyle)
                        .build())
                .forEach(prgOverlayRepository::save);

        if (syncGeoserver) {
            geoServerSynchronizer.synchronizeOverlays();
        }
    }
    @Operation(summary = "Synchronize Product layers")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Success", content = @Content),
            @ApiResponse(responseCode = "400", description = "Error occurred", content = @Content)
    })
    @PostMapping(path = "/product-layers/synchronize")
    public void synchronizeProductLayers() {
        log.info("Synchronizing Product layers");
        geoServerSynchronizer.synchronizeStoreAndMosaics();
    }

    @Operation(summary = "Create layer for Product")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Success", content = @Content),
            @ApiResponse(responseCode = "400", description = "Error occurred", content = @Content)
    })
    @PostMapping(path = "/product-layers/{layerName}")
    public void createProductLayer(@PathVariable String layerName) {
        log.info("Adding store and layer '" + layerName + "'");
        geoServerService.addStoreAndLayer(layerName);
    }
}
