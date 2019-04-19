package pl.cyfronet.s4e.service;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import pl.cyfronet.s4e.BasicTest;
import pl.cyfronet.s4e.bean.Product;
import pl.cyfronet.s4e.bean.ProductType;
import pl.cyfronet.s4e.bean.SldStyle;
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
    private GeoServerOperations geoServerOperations;

    @Autowired
    private GeoServerService geoServerService;

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
}
