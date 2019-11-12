package pl.cyfronet.s4e.controller;

import lombok.val;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import pl.cyfronet.s4e.BasicTest;
import pl.cyfronet.s4e.bean.Product;
import pl.cyfronet.s4e.bean.ProductType;
import pl.cyfronet.s4e.data.repository.ProductRepository;
import pl.cyfronet.s4e.data.repository.ProductTypeRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static pl.cyfronet.s4e.Constants.API_PREFIX_V1;
import static org.hamcrest.Matchers.*;

@AutoConfigureMockMvc
@BasicTest
public class ProductControllerTest {
    @Autowired
    private ProductTypeRepository productTypeRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    public void beforeEach() {
        resetProducts();
    }

    @AfterEach
    public void afterEach() {
        resetProducts();
    }

    private void resetProducts() {
        productRepository.deleteAll();
        productTypeRepository.deleteAll();
    }

    @Test
    public void shouldReturnZuluZonedTimestamp() throws Exception {
        val productType = productTypeRepository.save(ProductType.builder()
                .name("108m")
                .description("sth")
                .build());
        productRepository.save(Product.builder()
                .productType(productType)
                .layerName("testLayerName")
                .timestamp(LocalDateTime.of(2019, 10, 11, 12, 13))
                .s3Path("some/path")
                .build());

        mockMvc.perform(get(API_PREFIX_V1 + "/products/productTypeId/" + productType.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(equalTo(1))))
                .andExpect(jsonPath("$[0].timestamp").value(is(equalTo("2019-10-11T12:13:00Z"))));
    }

    @Test
    public void shouldReturnProducts() throws Exception {
        val productType = productTypeRepository.save(ProductType.builder()
                .name("108m")
                .description("sth")
                .build());

        val products = List.of(
                Product.builder()
                        .productType(productType)
                        .layerName("testLayerName1")
                        .timestamp(LocalDateTime.of(2019, 10, 1, 0, 0))
                        .s3Path("some/path")
                        .build(),
                Product.builder()
                        .productType(productType)
                        .layerName("testLayerName2")
                        .timestamp(LocalDateTime.of(2019, 10, 31, 23, 59, 59))
                        .s3Path("some/path")
                        .build(),
                Product.builder()
                        .productType(productType)
                        .layerName("testLayerName3")
                        .timestamp(LocalDateTime.of(2019, 11, 1, 0, 0))
                        .s3Path("some/path")
                        .build()
        );
        productRepository.saveAll(products);

        mockMvc.perform(get(API_PREFIX_V1 + "/products/productTypeId/" + productType.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(equalTo(3))))
                .andExpect(jsonPath("$..id", contains(
                        equalTo(products.get(0).getId().intValue()),
                        equalTo(products.get(1).getId().intValue()),
                        equalTo(products.get(2).getId().intValue()))));
    }

    @Test
    public void shouldReturnFilteredProducts() throws Exception {
        val productType = productTypeRepository.save(ProductType.builder()
                .name("108m")
                .description("sth")
                .build());

        val products = List.of(
                Product.builder()
                        .productType(productType)
                        .layerName("testLayerName1")
                        .timestamp(LocalDateTime.of(2019, 10, 1, 0, 0))
                        .s3Path("some/path")
                        .build(),
                Product.builder()
                        .productType(productType)
                        .layerName("testLayerName2")
                        .timestamp(LocalDateTime.of(2019, 10, 1, 23, 59, 59))
                        .s3Path("some/path")
                        .build(),
                Product.builder()
                        .productType(productType)
                        .layerName("testLayerName3")
                        .timestamp(LocalDateTime.of(2019, 10, 2, 0, 0))
                        .s3Path("some/path")
                        .build()
        );
        productRepository.saveAll(products);

        mockMvc.perform(get(API_PREFIX_V1 + "/products/productTypeId/" + productType.getId())
                .param("date", "2019-10-01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(equalTo(2))))
                .andExpect(jsonPath("$..id", contains(
                        equalTo(products.get(0).getId().intValue()),
                        equalTo(products.get(1).getId().intValue()))));
    }
}

