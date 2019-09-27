package pl.cyfronet.s4e.db;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import pl.cyfronet.s4e.BasicTest;
import pl.cyfronet.s4e.bean.Product;
import pl.cyfronet.s4e.bean.ProductType;
import pl.cyfronet.s4e.data.repository.ProductRepository;
import pl.cyfronet.s4e.data.repository.ProductTypeRepository;

import java.time.LocalDateTime;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@BasicTest
public class DbTimestampTest {
    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductTypeRepository productTypeRepository;

    @BeforeEach
    public void beforeEach() {
        productRepository.deleteAll();
        productTypeRepository.deleteAll();
    }

    @Test
    public void shouldSaveTimestampWithoutDSTCorrections() {
        val productType = productTypeRepository.save(ProductType.builder()
                .name("testProductType")
                .build());

        // A datetime, which if Polish timezone is used gets shifted one hour forward
        // on writing to DB.
        LocalDateTime dstBorderTimestamp = LocalDateTime.of(2019, 3, 31, 2, 0);
        val product = productRepository.save(Product.builder()
                .productType(productType)
                .layerName("testLayerName")
                .timestamp(dstBorderTimestamp)
                .s3Path("some/path")
                .build());

        val productId = productRepository.save(product).getId();

        val retrievedProduct = productRepository.findById(productId).get();
        assertThat(retrievedProduct.getTimestamp(), is(equalTo(dstBorderTimestamp)));
    }
}
