package pl.cyfronet.s4e.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import pl.cyfronet.s4e.BasicTest;
import pl.cyfronet.s4e.bean.Legend;
import pl.cyfronet.s4e.bean.Product;
import pl.cyfronet.s4e.data.repository.ProductRepository;
import pl.cyfronet.s4e.data.repository.SceneRepository;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private ProductRepository repository;

    @Autowired
    private SceneRepository sceneRepository;

    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    public void beforeEach() {
        resetProducts();
    }

    private void resetProducts() {
        sceneRepository.deleteAll();
        repository.deleteAll();
        Map<String,String> leftDesc = new HashMap<>();
        leftDesc.put("0.75","upper text left");
        leftDesc.put("0.25","lower text left");
        Map<String,String> rightDesc = new HashMap<>();
        rightDesc.put("0.75","upper text right");
        rightDesc.put("0.25","lower text right");
        Legend legend = Legend.builder()
                .type("gradient")
                .url("url")
                .leftDescription(leftDesc)
                .rightDescription(rightDesc)
                .build();
        List<Product> products = Arrays.asList(Product.builder()
                        .name("108m")
                        .description("Obraz satelitarny Meteosat dla obszaru Europy w kanale 10.8 µm z zastosowanie maskowanej palety barw dla obszarów mórz i lądów.")
                        .build(),
                Product.builder()
                        .name("Setvak")
                        .description("Obraz satelitarny Meteosat w kanale 10.8 µm z paletą barwną do analizy powierzchni wysokich chmur konwekcyjnych – obszar Europy Centralnej.")
                        .build(),
                Product.builder()
                        .name("WV-IR")
                        .description("Opis produktu WV-IR.")
                        .legend(legend)
                        .build());
        repository.saveAll(products);
    }

    @Test
    public void shouldReturnProductWithoutInfo() throws Exception {
        mockMvc.perform(get(API_PREFIX_V1 + "/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(equalTo(3))))
                .andExpect(jsonPath("$[0].description").doesNotExist());
    }

    @Test
    public void shouldReturnProductWithInfo() throws Exception {
        Product product = repository.findByNameContainingIgnoreCase("WV-IR").orElseThrow();
        ResultActions result = mockMvc.perform(get(API_PREFIX_V1 + "/products/" + product.getId()));
        result
                .andExpect(status().isOk())
                .andExpect(jsonPath("name", is(equalTo("WV-IR"))))
                .andExpect(jsonPath("description", is(equalTo("<p>Opis produktu WV-IR.</p>\n"))))
                .andExpect(jsonPath("$.legend['type']", is("gradient")))
                .andExpect(jsonPath("$.legend['url']", is("url")))
                .andExpect(jsonPath("$.legend['leftDescription']").exists())
                .andExpect(jsonPath("$.legend['bottomMetric']").doesNotExist());
    }
}

