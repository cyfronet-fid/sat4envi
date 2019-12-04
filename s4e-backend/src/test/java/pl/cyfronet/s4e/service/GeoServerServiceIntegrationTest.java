package pl.cyfronet.s4e.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;
import pl.cyfronet.s4e.BasicTest;
import pl.cyfronet.s4e.bean.PRGOverlay;
import pl.cyfronet.s4e.bean.Scene;
import pl.cyfronet.s4e.bean.ProductType;
import pl.cyfronet.s4e.bean.SldStyle;
import pl.cyfronet.s4e.data.repository.PRGOverlayRepository;
import pl.cyfronet.s4e.data.repository.SceneRepository;
import pl.cyfronet.s4e.data.repository.ProductTypeRepository;
import pl.cyfronet.s4e.data.repository.SldStyleRepository;
import pl.cyfronet.s4e.geoserver.op.GeoServerOperations;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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
    private SceneRepository sceneRepository;

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
        sceneRepository.deleteAll();
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
        Scene scene = sceneRepository.save(
                Scene.builder()
                        .productType(productType)
                        .timestamp(LocalDateTime.now())
                        .layerName("testLayerName")
                        .s3Path("201810042345_Merkator_WV-IR.tif")
                        .build());

        geoServerService.addLayer(scene);

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
    }

    @Transactional
    @Test
    public void shouldCreatePrgLayers() {
        geoServerService.resetWorkspace();
        SldStyle sldStyle = sldStyleRepository.save(SldStyle.builder()
                .name("wojewodztwa")
                .build());
        geoServerService.addStyle(sldStyle);
        sldStyle.setCreated(true);
        PRGOverlay prgOverlay = prgOverlayRepository.save(PRGOverlay.builder()
                .name("wojewodztwa")
                .featureType("wojew%C3%B3dztwa")
                .sldStyle(sldStyle)
                .build());

        List<PRGOverlay> prgOverlays = new ArrayList<>();
        prgOverlayRepository.findAll().forEach(prgOverlays::add);

        geoServerService.createPrgOverlays(prgOverlays);

        assertThat(geoServerOperations.layerExists("test", "wojew%C3%B3dztwa"), is(true));
        assertThat(geoServerOperations.getLayer("test", "wojew%C3%B3dztwa").getLayer().getDefaultStyle().getName(), is(equalTo("test:wojewodztwa")));
    }
}
