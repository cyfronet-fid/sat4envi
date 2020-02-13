package pl.cyfronet.s4e.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import pl.cyfronet.s4e.BasicTest;
import pl.cyfronet.s4e.Constants;
import pl.cyfronet.s4e.bean.PRGOverlay;
import pl.cyfronet.s4e.bean.Product;
import pl.cyfronet.s4e.bean.Scene;
import pl.cyfronet.s4e.bean.SldStyle;
import pl.cyfronet.s4e.data.repository.PRGOverlayRepository;
import pl.cyfronet.s4e.data.repository.ProductRepository;
import pl.cyfronet.s4e.data.repository.SceneRepository;
import pl.cyfronet.s4e.data.repository.SldStyleRepository;
import pl.cyfronet.s4e.geoserver.op.GeoServerOperations;
import pl.cyfronet.s4e.properties.GeoServerProperties;
import pl.cyfronet.s4e.util.S3AddressUtil;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

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
    private S3AddressUtil s3AddressUtil;

    @Autowired
    private GeoServerProperties geoServerProperties;

    @Mock
    private GeoServerOperations geoServerOperations;

    private GeoServerService geoServerService;
    private Product product;
    private Scene scene;
    private SldStyle sldStyle;
    private PRGOverlay prgOverlay;

    @BeforeEach
    public void beforeEach() {
        sceneRepository.deleteAll();
        productRepository.deleteAll();
        prgOverlayRepository.deleteAll();
        sldStyleRepository.deleteAll();
    }

    private void prepare() {
        geoServerService = new GeoServerService(
                geoServerOperations,
                s3AddressUtil,
                geoServerProperties);

        product = productRepository.save(
                Product.builder()
                        .name("productType")
                        .displayName("productType")
                        .build());
        scene = sceneRepository.save(
                Scene.builder()
                        .product(product)
                        .timestamp(LocalDateTime.now())
                        .layerName("testLayerName")
                        .s3Path("some/s3/path.tif")
                        .build());
        sldStyle = sldStyleRepository.save(
                SldStyle.builder()
                        .name("styleOne")
                        .build());
        prgOverlay = prgOverlayRepository.save(
                PRGOverlay.builder()
                        .name("wojewodztwa")
                        .featureType("wojewodztwaFeatureType")
                        .sldStyle(sldStyle)
                        .build());
    }

    @Test
    public void shouldAddLayerAndSetCreatedFlag() {
        prepare();

        geoServerService.addLayer(scene);

        verify(geoServerOperations, times(1)).createS3CoverageStore(geoServerProperties.getWorkspace(), scene.getLayerName(), s3AddressUtil.getS3Address(scene.getS3Path()));
        verify(geoServerOperations, times(1)).createS3Coverage(geoServerProperties.getWorkspace(), scene.getLayerName(), scene.getLayerName());
        verifyNoMoreInteractions(geoServerOperations);
    }

    @Test
    public void shouldTryToRollbackWhenAddLayerThrows() {
        prepare();
        doThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST)).when(geoServerOperations).createS3CoverageStore(any(), any(), any());

        assertThrows(HttpClientErrorException.class, () -> geoServerService.addLayer(scene));

        verify(geoServerOperations, times(1)).createS3CoverageStore(geoServerProperties.getWorkspace(), scene.getLayerName(), s3AddressUtil.getS3Address(scene.getS3Path()));
        verify(geoServerOperations, times(1)).deleteCoverageStore(geoServerProperties.getWorkspace(), scene.getLayerName(), true);
        verifyNoMoreInteractions(geoServerOperations);
    }

    @Test
    public void shouldAddStyleAndSetCreatedFlag() {
        prepare();

        geoServerService.addStyle(sldStyle);

        verify(geoServerOperations, times(1)).createStyle(geoServerProperties.getWorkspace(), sldStyle.getName());
        verify(geoServerOperations, times(1)).uploadSld(geoServerProperties.getWorkspace(), sldStyle.getName(), sldStyle.getName());
        verifyNoMoreInteractions(geoServerOperations);
    }

    @Test
    public void shouldCreatePrgLayers() {
        prepare();
        sldStyle.setCreated(true);
        sldStyle = sldStyleRepository.save(sldStyle);
        when(geoServerOperations.layerExists(geoServerProperties.getWorkspace(), prgOverlay.getFeatureType())).thenReturn(true);

        List<PRGOverlay> prgOverlays = new ArrayList<>();
        prgOverlayRepository.findAll().forEach(prgOverlays::add);

        geoServerService.createPrgOverlays(prgOverlays);

        verify(geoServerOperations, times(1)).createExternalShpDataStore(geoServerProperties.getWorkspace(), Constants.GEOSERVER_PRG_DATA_STORE, "file://"+Constants.GEOSERVER_PRG_PATH);
        verify(geoServerOperations, times(1)).setLayerDefaultStyle(geoServerProperties.getWorkspace(), prgOverlay.getFeatureType(), sldStyle.getName());
    }

    @Test
    public void shouldNotCreatePrgLayersIfAnyCreated() {
        prepare();
        prgOverlay.setCreated(true);
        prgOverlay = prgOverlayRepository.save(prgOverlay);

        List<PRGOverlay> prgOverlays = new ArrayList<>();
        prgOverlayRepository.findAll().forEach(prgOverlays::add);

        geoServerService.createPrgOverlays(prgOverlays);

        verifyNoMoreInteractions(geoServerOperations);
    }

    @Test
    public void shouldThrowIfSldStyleNotCreatedForPrgOverlay() {
        prepare();

        List<PRGOverlay> prgOverlays = new ArrayList<>();
        prgOverlayRepository.findAll().forEach(prgOverlays::add);

        assertThrows(IllegalStateException.class, () -> geoServerService.createPrgOverlays(prgOverlays));

        verifyNoMoreInteractions(geoServerOperations);
    }
}
