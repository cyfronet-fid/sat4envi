package pl.cyfronet.s4e.service;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import pl.cyfronet.s4e.BasicTest;
import pl.cyfronet.s4e.bean.Product;
import pl.cyfronet.s4e.bean.ProductType;
import pl.cyfronet.s4e.bean.Webhook;
import pl.cyfronet.s4e.data.repository.ProductRepository;
import pl.cyfronet.s4e.data.repository.ProductTypeRepository;
import pl.cyfronet.s4e.ex.NotFoundException;
import pl.cyfronet.s4e.util.S3Util;

import java.time.LocalDateTime;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@BasicTest
@Slf4j
public class ProductServiceTest {

    @Autowired
    private ProductTypeRepository productTypeRepository;

    @Autowired
    private ProductRepository productRepository;

    private ProductService productService;

    @Mock
    private S3Util s3Util;

    private static String WEBHOOK_KEY = "s4e-test-1/test/201810042345_Merkator_WV-IR.tif";

    @BeforeEach
    public void setUp() {
        productService = new ProductService(productRepository, productTypeRepository, s3Util);
        productRepository.deleteAll();
        productTypeRepository.deleteAll();
    }

    @Test
    public void shouldReturnAllProducts() {
        ProductType productType = ProductType.builder()
                .name("testProductType")
                .build();
        productTypeRepository.save(productType);

        Product product = Product.builder()
                .productType(productType)
                .layerName("testLayerName")
                .timestamp(LocalDateTime.now())
                .s3Path("some/path")
                .build();

        assertThat(productRepository.count(), is(equalTo(0L)));

        productRepository.save(product);

        assertThat(productRepository.count(), is(equalTo(1L)));
    }

    @Test
    public void shouldSaveProduct(){
        ProductType productType = ProductType.builder()
                .name("testProductType")
                .build();
        productTypeRepository.save(productType);

        Product product = Product.builder()
                .productType(productType)
                .layerName("testLayerName")
                .timestamp(LocalDateTime.now())
                .s3Path("some/path")
                .build();

        assertThat(productRepository.count(), is(equalTo(0L)));

        productService.saveProduct(product);

        assertThat(productRepository.count(), is(equalTo(1L)));
    }

    @Test
    public void shouldBuildFromWebhook() throws NotFoundException {
        Webhook webhook  = Webhook.builder()
                .eventName("EventName")
                .key(WEBHOOK_KEY)
                .build();
        ProductType productType = ProductType.builder()
                .name("WV-IR")
                .build();
        productTypeRepository.save(productType);
        when(s3Util.getProductType(anyString())).thenReturn("WV-IR");

        Product buildFromWebhook = productService.buildFromWebhook(webhook);
        assertThat(buildFromWebhook.getProductType().getName(), is(equalTo("WV-IR")));
    }

    @Test
    public void shouldReturnProductType() throws NotFoundException {
        ProductType productType = ProductType.builder()
                .name("WV-IR")
                .build();
        productTypeRepository.save(productType);
        when(s3Util.getProductType(anyString())).thenReturn("WV-IR");
        ProductType fromDb = productService.getProductType(WEBHOOK_KEY);
        assertThat(fromDb.getName(), is(equalTo("WV-IR")));
    }

}
