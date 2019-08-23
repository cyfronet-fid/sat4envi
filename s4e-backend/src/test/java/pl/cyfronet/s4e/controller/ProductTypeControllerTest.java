package pl.cyfronet.s4e.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import pl.cyfronet.s4e.BasicTest;
import pl.cyfronet.s4e.bean.Place;
import pl.cyfronet.s4e.bean.ProductType;
import pl.cyfronet.s4e.data.repository.PlaceRepository;
import pl.cyfronet.s4e.data.repository.ProductRepository;
import pl.cyfronet.s4e.data.repository.ProductTypeRepository;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static pl.cyfronet.s4e.Constants.API_PREFIX_V1;

@AutoConfigureMockMvc
@BasicTest
public class ProductTypeControllerTest {
    @Autowired
    private ProductTypeRepository repository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    public void beforeEach() {
        resetProductTypes();
    }

    private void resetProductTypes() {
        productRepository.deleteAll();
        repository.deleteAll();
        List<ProductType> productTypes = Arrays.asList(ProductType.builder()
                        .name("108m")
                        .description("Obraz satelitarny Meteosat dla obszaru Europy w kanale 10.8 µm z zastosowanie maskowanej palety barw dla obszarów mórz i lądów.")
                        .build(),
                ProductType.builder()
                        .name("Setvak")
                        .description("Obraz satelitarny Meteosat w kanale 10.8 µm z paletą barwną do analizy powierzchni wysokich chmur konwekcyjnych – obszar Europy Centralnej.")
                        .build(),
                ProductType.builder()
                        .name("WV-IR")
                        .description("Opis produktu WV-IR.")
                        .build());
        repository.saveAll(productTypes);
    }

    @Test
    public void shouldReturnProductTypeWithoutInfo() throws Exception {
        mockMvc.perform(get(API_PREFIX_V1 + "/productTypes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(equalTo(3))))
                .andExpect(jsonPath("$[0].description").doesNotExist());
    }

    @Test
    public void shouldReturnProductTypeWithInfo() throws Exception {
        ProductType productType = repository.findByNameContainingIgnoreCase("WV-IR").orElseThrow();
        mockMvc.perform(get(API_PREFIX_V1 + "/productTypes/" + productType.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("name", is(equalTo("WV-IR"))))
                .andExpect(jsonPath("description", is(equalTo("Opis produktu WV-IR."))));
    }
}

