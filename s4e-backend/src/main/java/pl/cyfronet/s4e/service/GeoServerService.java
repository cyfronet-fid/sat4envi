package pl.cyfronet.s4e.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientResponseException;
import pl.cyfronet.s4e.Constants;
import pl.cyfronet.s4e.bean.PRGOverlay;
import pl.cyfronet.s4e.bean.Product;
import pl.cyfronet.s4e.bean.SldStyle;
import pl.cyfronet.s4e.util.S3AddressUtil;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class GeoServerService {
    private final GeoServerOperations geoServerOperations;
    private final ProductService productService;
    private final SldStyleService sldStyleService;
    private final PRGOverlayService prgOverlayService;
    private final S3AddressUtil s3AddressUtil;

    @Value("${geoserver.workspace}")
    private String workspace;

    public void resetWorkspace() {
        try {
            geoServerOperations.deleteWorkspace(workspace, true);
        } catch (HttpClientErrorException.NotFound e) {
            // ignore
        }
        geoServerOperations.createWorkspace(workspace);
    }

    public void addLayer(Product product) {
        // Both the coverage store, coverage and layer (the last one with workspace prefix) names will be the same
        String gsName = product.getLayerName();
        try {
            geoServerOperations.createS3CoverageStore(workspace, gsName, s3AddressUtil.getS3Address(product.getS3Path()));
            geoServerOperations.createS3Coverage(workspace, gsName, gsName);
            productService.updateLayerCreated(product.getId(), true);
        } catch (RestClientResponseException e) {
            // try to clean up GeoServer state
            log.warn("Error when adding product", e);
            try {
                geoServerOperations.deleteCoverageStore(workspace, gsName, true);
            } catch (HttpClientErrorException.NotFound e1) {
                log.warn("Probably coverage store wasn't created", e1);
            } catch (RestClientResponseException e1) {
                log.error("Couldn't clean up GeoServer state", e1);
            }
            throw e;
        }
    }

    public void addStyle(SldStyle sldStyle) {
        String sldName = sldStyle.getName();
        try {
            geoServerOperations.createStyle(workspace, sldName);
            geoServerOperations.uploadSld(workspace, sldName, sldName);
            sldStyleService.updateCreated(sldStyle.getId(), true);
        } catch (RestClientResponseException e) {
            // try to clean up GeoServer state
            log.warn("Error when adding SLD Style", e);
            try {
                geoServerOperations.deleteStyle(workspace, sldName);
            } catch (HttpClientErrorException.NotFound e1) {
                log.warn("Probably SLD Style wasn't created", e1);
            } catch (RestClientResponseException e1) {
                log.error("Couldn't clean up GeoServer state", e1);
            }
            throw e;
        }
    }

    public void createPrgOverlays() {
        List<PRGOverlay> prgOverlays = prgOverlayService.getPRGOverlays();
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

        geoServerOperations.createExternalShpDataStore(workspace, Constants.GEOSERVER_PRG_DATA_STORE, "file://"+Constants.GEOSERVER_PRG_PATH);

        for (val prgOverlay: prgOverlays) {
            if (geoServerOperations.layerExists(workspace, prgOverlay.getFeatureType())) {
                prgOverlayService.updateCreated(prgOverlay.getId(), true);
                geoServerOperations.setLayerDefaultStyle(workspace, prgOverlay.getFeatureType(), prgOverlay.getSldStyle().getName());
            }
        }
    }
}
