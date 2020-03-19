package pl.cyfronet.s4e.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;
import pl.cyfronet.s4e.IntegrationTest;
import pl.cyfronet.s4e.bean.PRGOverlay;
import pl.cyfronet.s4e.bean.Product;
import pl.cyfronet.s4e.bean.SldStyle;
import pl.cyfronet.s4e.data.repository.PRGOverlayRepository;
import pl.cyfronet.s4e.data.repository.ProductRepository;
import pl.cyfronet.s4e.data.repository.SceneRepository;
import pl.cyfronet.s4e.data.repository.SldStyleRepository;
import pl.cyfronet.s4e.geoserver.op.GeoServerOperations;
import pl.cyfronet.s4e.geoserver.op.SeedProductsTest;
import pl.cyfronet.s4e.properties.GeoServerProperties;

import java.util.ArrayList;
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
    private GeoServerOperations geoServerOperations;

    @Autowired
    private GeoServerService geoServerService;

    @Autowired
    private SeedProductsTest seedProductsTest;

    @BeforeEach
    public void beforeEach() {
        seedProductsTest.preparedb();
        prgOverlayRepository.deleteAll();
        sldStyleRepository.deleteAll();

        geoServerService.resetWorkspace();
    }

    @AfterEach
    public void afterEach() {
        sceneRepository.deleteAll();
        productRepository.deleteAll();
        prgOverlayRepository.deleteAll();
        sldStyleRepository.deleteAll();
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
        PRGOverlay prgOverlay = prgOverlayRepository.save(PRGOverlay.builder()
                .name("wojewodztwa")
                .featureType("wojewodztwa")
                .sldStyle(sldStyle)
                .build());

        List<PRGOverlay> prgOverlays = new ArrayList<>();
        prgOverlayRepository.findAll().forEach(prgOverlays::add);

        geoServerService.createPrgOverlays(prgOverlays);

        assertThat(geoServerOperations.layerExists("test", "wojewodztwa"), is(true));
        assertThat(geoServerOperations.getLayer("test", "wojewodztwa").getLayer().getDefaultStyle().getName(), is(equalTo("test:wojewodztwa")));
    }

    @Test
    public void shouldAddStoreAndLayer() {
        Product product = productRepository.findByNameContainingIgnoreCase("108m").get();
        geoServerService.addStoreAndLayer(product);

        assertThat(geoServerOperations.layerExists("test", "108m"), is(true));
        assertThat(geoServerOperations.getLayer("test", "108m").getLayer().getName(),
                is(equalTo("108m")));
    }

    @Test
    public void shouldntAddStoreAndLayer() {
        Product product = Product.builder()
                .name("test")
                .layerName("test")
                .displayName("test")
                .description("Description")
                .build();
        Assertions.assertThrows(RestClientException.class, () -> geoServerService.addStoreAndLayer(product));
    }

    @Test
    public void shouldCheckIfLayerExists(){
        Product product = productRepository.findByNameContainingIgnoreCase("108m").get();
        geoServerService.addStoreAndLayer(product);

        assertThat(geoServerOperations.layerExists("test", "108m"), is(true));
        assertThat(geoServerOperations.layerExists("test", "108m12"), is(false));
    }
}
