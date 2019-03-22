package pl.cyfronet.s4e.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringRunner;
import pl.cyfronet.s4e.BasicTest;
import pl.cyfronet.s4e.bean.Product;
import pl.cyfronet.s4e.bean.ProductType;
import pl.cyfronet.s4e.data.repository.ProductRepository;
import pl.cyfronet.s4e.data.repository.ProductTypeRepository;

import java.time.LocalDateTime;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

@RunWith(SpringRunner.class)
@BasicTest
public class ProductServiceTest {

    @Autowired
    private ProductTypeRepository productTypeRepository;

    @Autowired
    private ProductRepository productRepository;

    @Before
    public void setUp() {
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
                .build();

        assertThat(productRepository.count(), is(equalTo(0L)));

        productRepository.save(product);

        assertThat(productRepository.count(), is(equalTo(1L)));
    }
}
