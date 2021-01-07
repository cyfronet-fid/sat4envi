/*
 * Copyright 2021 ACC Cyfronet AGH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package pl.cyfronet.s4e.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import pl.cyfronet.s4e.BasicTest;
import pl.cyfronet.s4e.TestDbHelper;
import pl.cyfronet.s4e.bean.*;
import pl.cyfronet.s4e.data.repository.*;

import java.util.*;

import static org.hamcrest.Matchers.*;
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
    private InstitutionRepository institutionRepository;

    @Autowired
    private UserRoleRepository userRoleRepository;

    @Autowired
    private LicenseGrantRepository licenseGrantRepository;

    @Autowired
    private TestDbHelper testDbHelper;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    private AppUser appUser;
    private Long productId;
    private List<Product> products;

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

        products = new ArrayList<>();
        repository.saveAll(Arrays.asList(Product.builder()
                        .name("108m")
                        .displayName("108m")
                        .description("Obraz satelitarny Meteosat dla obszaru Europy w kanale 10.8 µm z zastosowanie maskowanej palety barw dla obszarów mórz i lądów.")
                        .layerName("108m")
                        .granuleArtifactRule(Map.of())
                        .downloadOnly(false)
                        .authorizedOnly(false)
                        .accessType(Product.AccessType.OPEN)
                        .rank(3000L)
                        .build(),
                Product.builder()
                        .name("Setvak")
                        .displayName("Setvak")
                        .description("Obraz satelitarny Meteosat w kanale 10.8 µm z paletą barwną do analizy powierzchni wysokich chmur konwekcyjnych – obszar Europy Centralnej.")
                        .layerName("setvak")
                        .granuleArtifactRule(Map.of())
                        .downloadOnly(false)
                        .authorizedOnly(false)
                        .accessType(Product.AccessType.OPEN)
                        .rank(1000L)
                        .build(),
                Product.builder()
                        .name("WV_IR")
                        .displayName("WV-IR")
                        .description("Opis produktu WV-IR.")
                        .layerName("wv_ir")
                        .legend(legend)
                        .granuleArtifactRule(Map.of())
                        .downloadOnly(false)
                        .authorizedOnly(false)
                        .accessType(Product.AccessType.OPEN)
                        .rank(2000L)
                        .build(),
                Product.builder()
                        .name("unlisted")
                        .displayName("unlisted")
                        .description("unlisted")
                        .layerName("unlisted")
                        .legend(legend)
                        .granuleArtifactRule(Map.of())
                        .downloadOnly(true)
                        .authorizedOnly(false)
                        .accessType(Product.AccessType.OPEN)
                        .rank(4000L)
                        .build()))
                .forEach(products::add);

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

    @Nested
    class GetProducts {
        @BeforeEach
        public void beforeEach () {
            products.get(0).setAccessType(Product.AccessType.EUMETSAT);
            products.get(0).setAuthorizedOnly(true);
            products.get(1).setAccessType(Product.AccessType.PRIVATE);
            products.get(2).setAccessType(Product.AccessType.PRIVATE);
            repository.saveAll(products);
        }

        @Nested
        class ByUnauthenticatedUser {
            @Test
            public void shouldReturnNoProducts() throws Exception {
                mockMvc.perform(get(API_PREFIX_V1 + "/products"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$", hasSize(0)));
            }
        }

        @Nested
        class ByUserWithoutLicense {
            @Test
            public void shouldReturnOnlyEumetsatProduct() throws Exception {
                mockMvc.perform(get(API_PREFIX_V1 + "/products")
                        .with(jwtBearerToken(appUser, objectMapper)))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$[*].id", contains(
                                products.get(0).getId().intValue()
                        )));
            }
        }

        @Nested
        class ByUserWithLicense {
            @BeforeEach
            public void beforeEach() {
                val institution = institutionRepository.save(Institution.builder()
                        .name("test")
                        .slug("test")
                        .build());

                userRoleRepository.save(UserRole.builder().
                        role(AppRole.INST_MEMBER)
                        .user(appUser)
                        .institution(institution)
                        .build());

                licenseGrantRepository.save(LicenseGrant.builder()
                        .institution(institution)
                        .product(products.get(1))
                        .build());
            }

            @Test
            public void shouldReturnAlsoLicensedProducts() throws Exception {
                mockMvc.perform(get(API_PREFIX_V1 + "/products")
                        .with(jwtBearerToken(appUser, objectMapper)))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$[*].id", contains(
                                products.get(1).getId().intValue(),
                                products.get(0).getId().intValue()
                        )));
            }
        }

        @Nested
        class ByAdmin {
            @BeforeEach
            public void beforeEach() {
                appUser.addAuthority("ROLE_ADMIN");
                appUserRepository.save(appUser);
            }

            @Test
            public void shouldReturnAllProducts() throws Exception {
                mockMvc.perform(get(API_PREFIX_V1 + "/products")
                        .with(jwtBearerToken(appUser, objectMapper)))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$[*].id", contains(
                                products.get(1).getId().intValue(),
                                products.get(2).getId().intValue(),
                                products.get(0).getId().intValue()
                        )));
            }
        }
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
                .andExpect(jsonPath("$[2].favourite", is(equalTo(true))))
                .andExpect(jsonPath("$[2].description").doesNotExist());
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
                .andExpect(jsonPath("$[2].favourite", is(equalTo(true))))
                .andExpect(jsonPath("$[2].description").doesNotExist());
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
                .andExpect(jsonPath("$[2].favourite", is(equalTo(true))))
                .andExpect(jsonPath("$[2].description").doesNotExist());
        // now we remove
        mockMvc.perform(delete(API_PREFIX_V1 + "/products/{id}/favourite", product.getId())
                .with(jwtBearerToken(appUser, objectMapper)))
                .andExpect(status().isOk());

        mockMvc.perform(get(API_PREFIX_V1 + "/products")
                .with(jwtBearerToken(appUser, objectMapper)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(equalTo(3))))
                //$[0] is 108m product
                .andExpect(jsonPath("$[2].favourite", is(equalTo(false))))
                .andExpect(jsonPath("$[2].description").doesNotExist());
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

