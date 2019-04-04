package pl.cyfronet.s4e.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientResponseException;
import pl.cyfronet.s4e.bean.Product;
import pl.cyfronet.s4e.util.S3AddressUtil;

@Service
@RequiredArgsConstructor
@Slf4j
public class GeoServerService {
    private final GeoServerOperations geoServerOperations;
    private final ProductService productService;
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
}
