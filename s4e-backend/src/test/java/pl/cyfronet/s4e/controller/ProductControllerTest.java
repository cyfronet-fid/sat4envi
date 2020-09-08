package pl.cyfronet.s4e.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import pl.cyfronet.s4e.BasicTest;
import pl.cyfronet.s4e.TestDbHelper;
import pl.cyfronet.s4e.bean.AppUser;
import pl.cyfronet.s4e.bean.Legend;
import pl.cyfronet.s4e.bean.Product;
import pl.cyfronet.s4e.data.repository.AppUserRepository;
import pl.cyfronet.s4e.data.repository.ProductRepository;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static pl.cyfronet.s4e.Constants.API_PREFIX_V1;
import static pl.cyfronet.s4e.TestJwtUtil.jwtBearerToken;

@AutoConfigureMockMvc
@BasicTest
public class ProductControllerTest {
    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private ProductRepository repository;

    @Autowired
    private TestDbHelper testDbHelper;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    private AppUser appUser;
    private Long productId;

    @BeforeEach
    public void beforeEach() {
        resetDb();

        appUser = appUserRepository.save(AppUser.builder()
                .email("get@profile.com")
                .name("Get")
                .surname("Profile")
                .password("{noop}password")
                .enabled(true)
                .build());

        Map<String, String> leftDesc = new HashMap<>();
        leftDesc.put("0.75", "upper text left");
        leftDesc.put("0.25", "lower text left");
        Map<String, String> rightDesc = new HashMap<>();
        rightDesc.put("0.75", "upper text right");
        rightDesc.put("0.25", "lower text right");
        Legend legend = Legend.builder()
                .type("gradient")
                .url("url")
                .leftDescription(leftDesc)
                .rightDescription(rightDesc)
                .build();

        repository.saveAll(Arrays.asList(Product.builder()
                        .name("108m")
                        .displayName("108m")
                        .description("Obraz satelitarny Meteosat dla obszaru Europy w kanale 10.8 µm z zastosowanie maskowanej palety barw dla obszarów mórz i lądów.")
                        .layerName("108m")
                        .granuleArtifactRule(Map.of())
                        .build(),
                Product.builder()
                        .name("Setvak")
                        .displayName("Setvak")
                        .description("Obraz satelitarny Meteosat w kanale 10.8 µm z paletą barwną do analizy powierzchni wysokich chmur konwekcyjnych – obszar Europy Centralnej.")
                        .layerName("setvak")
                        .granuleArtifactRule(Map.of())
                        .build(),
                Product.builder()
                        .name("WV_IR")
                        .displayName("WV-IR")
                        .description("Opis produktu WV-IR.")
                        .layerName("wv_ir")
                        .legend(legend)
                        .granuleArtifactRule(Map.of())
                        .build()));

        productId = repository.findByNameContainingIgnoreCase("108m").get().getId();
    }

    @AfterEach
    public void afterEach() {
        resetDb();
    }

    private void resetDb() {
        testDbHelper.clean();
    }

    @Test
    public void shouldReturnProductWithInfo() throws Exception {
        Product product = repository.findByNameContainingIgnoreCase("WV_IR").orElseThrow();
        val url = API_PREFIX_V1 + "/products/{id}";
        mockMvc.perform(get(url, product.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("name", is(equalTo("WV_IR"))))
                .andExpect(jsonPath("displayName", is(equalTo("WV-IR"))))
                .andExpect(jsonPath("description", is(equalTo("<p>Opis produktu WV-IR.</p>\n"))))
                .andExpect(jsonPath("$.legend['type']", is("gradient")))
                .andExpect(jsonPath("$.legend['url']", is("url")))
                .andExpect(jsonPath("$.legend['leftDescription']").exists())
                .andExpect(jsonPath("$.legend['bottomMetric']").doesNotExist());
    }

    @Test
    public void shouldReturnProductWithoutInfoForUnauthorizedUser() throws Exception {
        mockMvc.perform(get(API_PREFIX_V1 + "/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(equalTo(3))))
                .andExpect(jsonPath("$[0].favourite", is(equalTo(false))))
                .andExpect(jsonPath("$[0].description").doesNotExist());
    }

    @Test
    public void shouldReturnProductWithoutInfoForAuthorizedUserWithFavourite() throws Exception {
        Product product = repository.findByNameContainingIgnoreCase("108m").get();
        product.setFavourites(Set.of(appUser));
        repository.save(product);

        mockMvc.perform(get(API_PREFIX_V1 + "/products")
                .with(jwtBearerToken(appUser, objectMapper)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(equalTo(3))))
                //$[0] is 108m product
                .andExpect(jsonPath("$[0].favourite", is(equalTo(true))))
                .andExpect(jsonPath("$[0].description").doesNotExist());
    }

    @Test
    public void shouldAddFavouriteProductForAuthorizedUser() throws Exception {
        Product product = repository.findByNameContainingIgnoreCase("108m").get();

        mockMvc.perform(put(API_PREFIX_V1 + "/products/{id}/favourite", product.getId())
                .with(jwtBearerToken(appUser, objectMapper)))
                .andExpect(status().isOk());

        mockMvc.perform(get(API_PREFIX_V1 + "/products")
                .with(jwtBearerToken(appUser, objectMapper)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(equalTo(3))))
                //$[0] is 108m product
                .andExpect(jsonPath("$[0].favourite", is(equalTo(true))))
                .andExpect(jsonPath("$[0].description").doesNotExist());
    }

    @Test
    public void shouldntAddFavouriteProductForUnauthorizedUser() throws Exception {
        mockMvc.perform(put(API_PREFIX_V1 + "/products/{id}/favourite", 1L))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void shouldntAddFavouriteProductForAuthorizedUserNotFound() throws Exception {
        mockMvc.perform(delete(API_PREFIX_V1 + "/products/{id}/favourite", -1L)
                .with(jwtBearerToken(appUser, objectMapper)))
                .andExpect(status().isNotFound());
    }

    @Test
    public void shouldRemoveFavouriteProductForAuthorizedUser() throws Exception {
        Product product = repository.findByNameContainingIgnoreCase("108m").get();

        mockMvc.perform(put(API_PREFIX_V1 + "/products/{id}/favourite", product.getId())
                .with(jwtBearerToken(appUser, objectMapper)))
                .andExpect(status().isOk());

        mockMvc.perform(get(API_PREFIX_V1 + "/products")
                .with(jwtBearerToken(appUser, objectMapper)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(equalTo(3))))
                //$[0] is 108m product
                .andExpect(jsonPath("$[0].favourite", is(equalTo(true))))
                .andExpect(jsonPath("$[0].description").doesNotExist());
        // now we remove
        mockMvc.perform(delete(API_PREFIX_V1 + "/products/{id}/favourite", product.getId())
                .with(jwtBearerToken(appUser, objectMapper)))
                .andExpect(status().isOk());

        mockMvc.perform(get(API_PREFIX_V1 + "/products")
                .with(jwtBearerToken(appUser, objectMapper)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(equalTo(3))))
                //$[0] is 108m product
                .andExpect(jsonPath("$[0].favourite", is(equalTo(false))))
                .andExpect(jsonPath("$[0].description").doesNotExist());
    }

    @Test
    public void shouldntRemoveFavouriteProductForUnauthorizedUser() throws Exception {
        mockMvc.perform(delete(API_PREFIX_V1 + "/products/{id}/favourite", 1L))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void shouldntRemoveFavouriteProductForAuthorizedUserNotFound() throws Exception {
        mockMvc.perform(delete(API_PREFIX_V1 + "/products/{id}/favourite", -1L)
                .with(jwtBearerToken(appUser, objectMapper)))
                .andExpect(status().isNotFound());
    }

    @Test
    public void shouldReturn200ForMultipleAddAndRemoveOfFavourite() throws Exception {
        mockMvc.perform(put(API_PREFIX_V1 + "/products/{id}/favourite", productId)
                .with(jwtBearerToken(appUser, objectMapper)))
                .andExpect(status().isOk());

        mockMvc.perform(put(API_PREFIX_V1 + "/products/{id}/favourite", productId)
                .with(jwtBearerToken(appUser, objectMapper)))
                .andExpect(status().isOk());

        mockMvc.perform(delete(API_PREFIX_V1 + "/products/{id}/favourite", productId)
                .with(jwtBearerToken(appUser, objectMapper)))
                .andExpect(status().isOk());

        mockMvc.perform(delete(API_PREFIX_V1 + "/products/{id}/favourite", productId)
                .with(jwtBearerToken(appUser, objectMapper)))
                .andExpect(status().isOk());
    }
}

