package pl.cyfronet.s4e.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import pl.cyfronet.s4e.BasicTest;
import pl.cyfronet.s4e.bean.PRGOverlay;
import pl.cyfronet.s4e.bean.Product;
import pl.cyfronet.s4e.bean.ProductType;
import pl.cyfronet.s4e.bean.SldStyle;
import pl.cyfronet.s4e.data.repository.PRGOverlayRepository;
import pl.cyfronet.s4e.data.repository.ProductRepository;
import pl.cyfronet.s4e.data.repository.ProductTypeRepository;
import pl.cyfronet.s4e.data.repository.SldStyleRepository;

import java.time.LocalDateTime;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@BasicTest
@Tag("integration")
public class GeoServerServiceIntegrationTest {

    @Value("${geoserver.workspace}")
    private String workspace;

    @Autowired
    private ProductTypeRepository productTypeRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private SldStyleRepository sldStyleRepository;

    @Autowired
    private PRGOverlayRepository prgOverlayRepository;

    @Autowired
    private GeoServerOperations geoServerOperations;

    @Autowired
    private GeoServerService geoServerService;

    @BeforeEach
    public void beforeEach() {
        productRepository.deleteAll();
        productTypeRepository.deleteAll();
        prgOverlayRepository.deleteAll();
        sldStyleRepository.deleteAll();
    }

    @Test
    public void shouldCreateLayer() {
        geoServerService.resetWorkspace();
        ProductType productType = productTypeRepository.save(
                ProductType.builder()
                        .name("productType")
                        .build());
        Product product = productRepository.save(
                Product.builder()
                        .productType(productType)
                        .timestamp(LocalDateTime.now())
                        .layerName("testLayerName")
                        .s3Path("201810042345_Merkator_WV-IR.tif")
                        .build());

        geoServerService.addLayer(product);

        assertThat(geoServerOperations.listCoverages(workspace, "testLayerName"), contains("testLayerName"));
    }

    @Test
    public void shouldCreateSldStyle() {
        geoServerService.resetWorkspace();
        SldStyle sldStyle = sldStyleRepository.save(SldStyle.builder()
                .name("styleOne")
                .build());

        geoServerService.addStyle(sldStyle);

        assertThat(geoServerOperations.listStyles(workspace), contains("styleOne"));

        SldStyle updatedSldStyle = sldStyleRepository.findById(sldStyle.getId()).get();
        assertThat(updatedSldStyle.isCreated(), is(true));
    }

    @Test
    public void shouldCreatePrgLayers() {
        geoServerService.resetWorkspace();
        SldStyle sldStyle = sldStyleRepository.save(SldStyle.builder()
                .name("wojewodztwa")
                .build());
        geoServerService.addStyle(sldStyle);
        sldStyle = sldStyleRepository.findById(sldStyle.getId()).get();
        PRGOverlay prgOverlay = prgOverlayRepository.save(PRGOverlay.builder()
                .name("wojewodztwa")
                .featureType("wojew%C3%B3dztwa")
                .sldStyle(sldStyle)
                .build());

        assertThat(sldStyle.isCreated(), is(true));
        assertThat(prgOverlay.isCreated(), is(false));

        geoServerService.createPrgOverlays();

        PRGOverlay updatedPrgOverlay = prgOverlayRepository.findById(prgOverlay.getId()).get();
        assertThat(updatedPrgOverlay.isCreated(), is(true));
        assertThat(geoServerOperations.layerExists("test", "wojew%C3%B3dztwa"), is(true));
        assertThat(geoServerOperations.getLayer("test", "wojew%C3%B3dztwa").getLayer().getDefaultStyle().getName(), is(equalTo("test:wojewodztwa")));
    }
}
