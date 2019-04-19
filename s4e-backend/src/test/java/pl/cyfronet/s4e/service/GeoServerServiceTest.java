package pl.cyfronet.s4e.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;
import pl.cyfronet.s4e.BasicTest;
import pl.cyfronet.s4e.Constants;
import pl.cyfronet.s4e.bean.PRGOverlay;
import pl.cyfronet.s4e.bean.Product;
import pl.cyfronet.s4e.bean.ProductType;
import pl.cyfronet.s4e.bean.SldStyle;
import pl.cyfronet.s4e.data.repository.PRGOverlayRepository;
import pl.cyfronet.s4e.data.repository.ProductRepository;
import pl.cyfronet.s4e.data.repository.ProductTypeRepository;
import pl.cyfronet.s4e.data.repository.SldStyleRepository;
import pl.cyfronet.s4e.util.S3AddressUtil;

import java.time.LocalDateTime;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@BasicTest
class GeoServerServiceTest {

    @Autowired
    private ProductTypeRepository productTypeRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private SldStyleRepository sldStyleRepository;

    @Autowired
    private PRGOverlayRepository prgOverlayRepository;

    @Autowired
    private ProductService productService;

    @Autowired
    private SldStyleService sldStyleService;

    @Autowired
    private PRGOverlayService prgOverlayService;

    @Autowired
    private S3AddressUtil s3AddressUtil;

    @Mock
    private GeoServerOperations geoServerOperations;

    @Value("${geoserver.workspace}")
    private String workspace;

    private GeoServerService geoServerService;
    private ProductType productType;
    private Product product;
    private SldStyle sldStyle;
    private PRGOverlay prgOverlay;

    @BeforeEach
    public void beforeEach() {
        productRepository.deleteAll();
        productTypeRepository.deleteAll();
        prgOverlayRepository.deleteAll();
        sldStyleRepository.deleteAll();
    }

    private void prepare() {
        geoServerService = new GeoServerService(geoServerOperations, productService, sldStyleService, prgOverlayService, s3AddressUtil);
        ReflectionTestUtils.setField(geoServerService, "workspace", workspace);

        productType = productTypeRepository.save(
                ProductType.builder()
                        .name("productType")
                        .build());
        product = productRepository.save(
                Product.builder()
                        .productType(productType)
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

        assertThat(product.isLayerCreated(), is(equalTo(false)));

        geoServerService.addLayer(product);

        Product updatedProduct = productRepository.findById(product.getId()).get();
        assertThat(updatedProduct.isLayerCreated(), is(equalTo(true)));
        verify(geoServerOperations, times(1)).createS3CoverageStore(workspace, product.getLayerName(), s3AddressUtil.getS3Address(product.getS3Path()));
        verify(geoServerOperations, times(1)).createS3Coverage(workspace, product.getLayerName(), product.getLayerName());
        verifyNoMoreInteractions(geoServerOperations);
    }

    @Test
    public void shouldTryToRollbackWhenAddLayerThrows() {
        prepare();
        doThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST)).when(geoServerOperations).createS3CoverageStore(any(), any(), any());

        assertThat(product.isLayerCreated(), is(equalTo(false)));

        assertThrows(HttpClientErrorException.class, () -> geoServerService.addLayer(product));

        Product updatedProduct = productRepository.findById(product.getId()).get();
        assertThat(updatedProduct.isLayerCreated(), is(equalTo(false)));
        verify(geoServerOperations, times(1)).createS3CoverageStore(workspace, product.getLayerName(), s3AddressUtil.getS3Address(product.getS3Path()));
        verify(geoServerOperations, times(1)).deleteCoverageStore(workspace, product.getLayerName(), true);
        verifyNoMoreInteractions(geoServerOperations);
    }

    @Test
    public void shouldAddStyleAndSetCreatedFlag() {
        prepare();

        assertThat(sldStyle.isCreated(), is(equalTo(false)));

        geoServerService.addStyle(sldStyle);

        SldStyle updatedSldStyle = sldStyleRepository.findById(sldStyle.getId()).get();
        assertThat(updatedSldStyle.isCreated(), is(equalTo(true)));
        verify(geoServerOperations, times(1)).createStyle(workspace, sldStyle.getName());
        verify(geoServerOperations, times(1)).uploadSld(workspace, sldStyle.getName(), sldStyle.getName());
        verifyNoMoreInteractions(geoServerOperations);
    }

    @Test
    public void shouldCreatePrgLayers() {
        prepare();
        sldStyle.setCreated(true);
        sldStyle = sldStyleRepository.save(sldStyle);
        when(geoServerOperations.layerExists(workspace, prgOverlay.getFeatureType())).thenReturn(true);

        geoServerService.createPrgOverlays();

        PRGOverlay updatedPrgOverlay = prgOverlayRepository.findById(prgOverlay.getId()).get();
        assertThat(updatedPrgOverlay.isCreated(), is(true));
        verify(geoServerOperations, times(1)).createExternalShpDataStore(workspace, Constants.GEOSERVER_PRG_DATA_STORE, "file://"+Constants.GEOSERVER_PRG_PATH);
        verify(geoServerOperations, times(1)).setLayerDefaultStyle(workspace, prgOverlay.getFeatureType(), sldStyle.getName());
    }

    @Test
    public void shouldNotCreatePrgLayersIfAnyCreated() {
        prepare();
        prgOverlay.setCreated(true);
        prgOverlay = prgOverlayRepository.save(prgOverlay);

        geoServerService.createPrgOverlays();

        verifyNoMoreInteractions(geoServerOperations);
    }

    @Test
    public void shouldThrowIfSldStyleNotCreatedForPrgOverlay() {
        prepare();

        assertThrows(IllegalStateException.class, () -> geoServerService.createPrgOverlays());

        PRGOverlay updatedPrgOverlay = prgOverlayRepository.findById(prgOverlay.getId()).get();
        assertThat(updatedPrgOverlay.isCreated(), is(false));
        verifyNoMoreInteractions(geoServerOperations);
    }
}
