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

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static pl.cyfronet.s4e.Constants.API_PREFIX_V1;

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
}

