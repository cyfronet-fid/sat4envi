package pl.cyfronet.s4e.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import pl.cyfronet.s4e.BasicTest;
import pl.cyfronet.s4e.Constants;
import pl.cyfronet.s4e.TestDbHelper;
import pl.cyfronet.s4e.bean.PRGOverlay;
import pl.cyfronet.s4e.bean.Product;
import pl.cyfronet.s4e.bean.SldStyle;
import pl.cyfronet.s4e.data.repository.PRGOverlayRepository;
import pl.cyfronet.s4e.data.repository.ProductRepository;
import pl.cyfronet.s4e.data.repository.SceneRepository;
import pl.cyfronet.s4e.data.repository.SldStyleRepository;
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
        prgOverlay = prgOverlayRepository.save(prgOverlayBuilder(sldStyle).build());
    }

    private SldStyle.SldStyleBuilder sldStyleBuilder() {
        return SldStyle.builder()
                .name("styleOne");
    }

    private PRGOverlay.PRGOverlayBuilder prgOverlayBuilder(SldStyle sldStyle) {
        return PRGOverlay.builder()
                .name("wojewodztwa")
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

        verify(geoServerOperations, times(1)).createExternalShpDataStore(geoServerProperties.getWorkspace(), Constants.GEOSERVER_PRG_DATA_STORE, "file://"+Constants.GEOSERVER_PRG_PATH);
        verify(geoServerOperations, times(1)).setLayerDefaultStyle(geoServerProperties.getWorkspace(), prgOverlay.getFeatureType(), sldStyle.getName());
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
