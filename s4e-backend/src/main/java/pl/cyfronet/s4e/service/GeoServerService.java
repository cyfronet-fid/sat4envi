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

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientResponseException;
import pl.cyfronet.s4e.Constants;
import pl.cyfronet.s4e.bean.PRGOverlay;
import pl.cyfronet.s4e.bean.Product;
import pl.cyfronet.s4e.bean.SldStyle;
import pl.cyfronet.s4e.geoserver.op.GeoServerOperations;
import pl.cyfronet.s4e.properties.GeoServerProperties;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class GeoServerService {
    private final GeoServerOperations geoServerOperations;
    private final GeoServerProperties geoServerProperties;

    public void resetWorkspace() {
        try {
            geoServerOperations.deleteWorkspace(geoServerProperties.getWorkspace(), true);
        } catch (HttpClientErrorException.NotFound e) {
            // ignore
        }
        geoServerOperations.createWorkspace(geoServerProperties.getWorkspace());
    }

    @Transactional(rollbackFor = RestClientResponseException.class)
    public void addStoreAndLayer(Product product) {
        addStoreAndLayer(product.getLayerName());
    }

    @Transactional(rollbackFor = RestClientResponseException.class)
    public void addStoreAndLayer(String gsName) {
        // Both the coverage store, coverage and layer (the last one with workspace prefix) names will be the same
        try {
            geoServerOperations.createS3CoverageStore(geoServerProperties.getWorkspace(), gsName);
            geoServerOperations.createS3Coverage(geoServerProperties.getWorkspace(), gsName, gsName);
            geoServerOperations.createTileLayer(geoServerProperties.getWorkspace(), gsName);
        } catch (RestClientResponseException e) {
            // try to clean up GeoServer state
            log.warn("Error when adding product", e);
            try {
                geoServerOperations.deleteTileLayer(geoServerProperties.getWorkspace(), gsName);
            } catch (HttpServerErrorException.InternalServerError e1) {
                log.warn("Probably tile layer wasn't created", e1);
            } catch (RestClientResponseException e1) {
                log.error("Couldn't clean up GeoServer state", e1);
            } finally {
                try {
                    geoServerOperations.deleteCoverageStore(geoServerProperties.getWorkspace(), gsName, true);
                } catch (HttpClientErrorException.NotFound e1) {
                    log.warn("Probably coverage store wasn't created", e1);
                } catch (RestClientResponseException e1) {
                    log.error("Couldn't clean up GeoServer state", e1);
                }
            }
            throw e;
        }
    }

    public void addStyle(SldStyle sldStyle) {
        String sldName = sldStyle.getName();
        try {
            geoServerOperations.createStyle(geoServerProperties.getWorkspace(), sldName);
            geoServerOperations.uploadSld(geoServerProperties.getWorkspace(), sldName, sldName);
        } catch (RestClientResponseException e) {
            // try to clean up GeoServer state
            log.warn("Error when adding SLD Style", e);
            try {
                geoServerOperations.deleteStyle(geoServerProperties.getWorkspace(), sldName);
            } catch (HttpClientErrorException.NotFound e1) {
                log.warn("Probably SLD Style wasn't created", e1);
            } catch (RestClientResponseException e1) {
                log.error("Couldn't clean up GeoServer state", e1);
            }
            throw e;
        }
    }

    public void createPrgOverlays(List<PRGOverlay> prgOverlays) {
        if (prgOverlays.stream().anyMatch(PRGOverlay::isCreated)) {
            // the initialization procedure must've been run already
            return;
        }

        if (prgOverlays.stream().anyMatch(prgOverlay -> !prgOverlay.getSldStyle().isCreated())) {
            // create a list of missing styles in this form "<style1name>, <style2name>, <style4name>"
            String missingStyles = prgOverlays.stream()
                    .filter(prgOverlay -> !prgOverlay.getSldStyle().isCreated())
                    .map(prgOverlay -> prgOverlay.getSldStyle().getName())
                    .reduce((n1, n2) -> n1 + ", " + n2).get();
            throw new IllegalStateException("You are trying to configure PRGOverlays, but not all styles have been created yet. Create these styles first: "+missingStyles);
        }

        geoServerOperations.createExternalShpDataStore(geoServerProperties.getWorkspace(), Constants.GEOSERVER_PRG_DATA_STORE, "file://"+Constants.GEOSERVER_PRG_PATH);

        for (val prgOverlay: prgOverlays) {
            if (layerExists(prgOverlay.getFeatureType())) {
                geoServerOperations.setLayerDefaultStyle(geoServerProperties.getWorkspace(), prgOverlay.getFeatureType(), prgOverlay.getSldStyle().getName());
                geoServerOperations.createTileLayer(geoServerProperties.getWorkspace(), prgOverlay.getFeatureType());
            }
        }
    }

    public boolean layerExists(String layerName) {
        return geoServerOperations.layerExists(geoServerProperties.getWorkspace(), layerName);
    }
}
