/*
 * Copyright 2020 ACC Cyfronet AGH
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

package pl.cyfronet.s4e.admin.scene;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import pl.cyfronet.s4e.*;
import pl.cyfronet.s4e.bean.AppUser;
import pl.cyfronet.s4e.bean.Product;
import pl.cyfronet.s4e.bean.Scene;
import pl.cyfronet.s4e.data.repository.AppUserRepository;
import pl.cyfronet.s4e.data.repository.ProductRepository;
import pl.cyfronet.s4e.data.repository.SceneRepository;
import pl.cyfronet.s4e.data.repository.SchemaRepository;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static pl.cyfronet.s4e.TestJwtUtil.jwtBearerToken;

@BasicTest
@AutoConfigureMockMvc
public class AdminSceneControllerTest {
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SchemaRepository schemaRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private SceneRepository sceneRepository;

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TestDbHelper testDbHelper;

    @Autowired
    private TestResourceHelper testResourceHelper;

    private AppUser admin;

    private Map<String, Product> products;
    private Map<String, List<Scene>> productScenes;
    private long maxSceneId;

    @BeforeEach
    public void beforeEach() {
        testDbHelper.clean();

        admin = appUserRepository.save(AppUser.builder()
                .email("admin@mail.pl")
                .name("John")
                .surname("Smith")
                .password("{noop}password")
                .enabled(true)
                .authority("ROLE_ADMIN")
                .build());

        SchemaTestHelper.SCENE_AND_METADATA_SCHEMA_NAMES.stream()
                .map(path -> SchemaTestHelper.schemaBuilder(path, testResourceHelper).build())
                .forEach(schemaRepository::save);

        products = IntStream.range(0, 2)
                .mapToObj(i -> SceneTestHelper.productBuilder()
                        .name("Product-" + i)
                        .build())
                .map(productRepository::save)
                .collect(Collectors.toMap(Product::getName, product -> product));

        productScenes = new HashMap<>();
        for (Product product : products.values()) {
            Function<LocalDateTime, Scene> toScene = SceneTestHelper.toScene(product);
            List<Scene> scenes = IntStream.range(0, 10)
                    .mapToObj(i -> LocalDateTime.of(2020, 10, 7, 0, 0).plusHours(i))
                    .map(toScene)
                    .map(sceneRepository::save)
                    .collect(Collectors.toList());
            productScenes.put(product.getName(), scenes);
        }

        maxSceneId = productScenes.values().stream()
                .flatMap(List::stream)
                .mapToLong(Scene::getId)
                .max().orElse(Long.MIN_VALUE);
    }

    @Nested
    class Read {
        @Test
        public void shouldRead() throws Exception {
            assertThat(productRepository.count(), is(equalTo(2L)));
            assertThat(sceneRepository.count(), is(equalTo(20L)));

            Product product = products.get("Product-0");
            Scene scene = productScenes.get(product.getName()).get(0);

            mockMvc.perform(get(Constants.ADMIN_PREFIX + "/scenes/{id}", scene.getId())
                    .with(jwtBearerToken(admin, objectMapper)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").isNumber())
                    .andExpect(jsonPath("$.product.id", is(equalTo(product.getId().intValue()))))
                    .andExpect(jsonPath("$.product.name", is(equalTo(product.getName()))))
                    .andExpect(jsonPath("$.sceneKey").isString())
                    .andExpect(jsonPath("$.timestamp", is(equalTo("2020-10-07T00:00:00"))))
                    .andExpect(jsonPath("$.footprint.epsg3857").isString())
                    .andExpect(jsonPath("$.footprint.epsg4326").isString())
                    .andExpect(jsonPath("$.legend").isMap())
                    .andExpect(jsonPath("$.sceneContent").isMap())
                    .andExpect(jsonPath("$.metadataContent").isMap());

            assertThat(productRepository.count(), is(equalTo(2L)));
            assertThat(sceneRepository.count(), is(equalTo(20L)));
        }

        @Test
        public void shouldHandleNotFound() throws Exception {
            assertThat(productRepository.count(), is(equalTo(2L)));
            assertThat(sceneRepository.count(), is(equalTo(20L)));

            mockMvc.perform(get(Constants.ADMIN_PREFIX + "/scenes/{id}", maxSceneId + 1)
                    .with(jwtBearerToken(admin, objectMapper)))
                    .andExpect(status().isNotFound());

            assertThat(productRepository.count(), is(equalTo(2L)));
            assertThat(sceneRepository.count(), is(equalTo(20L)));
        }
    }

    @Nested
    class Lists {
        @Test
        public void shouldList() throws Exception {
            assertThat(productRepository.count(), is(equalTo(2L)));
            assertThat(sceneRepository.count(), is(equalTo(20L)));

            mockMvc.perform(get(Constants.ADMIN_PREFIX + "/scenes")
                    .with(jwtBearerToken(admin, objectMapper)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(20)));

            assertThat(productRepository.count(), is(equalTo(2L)));
            assertThat(sceneRepository.count(), is(equalTo(20L)));
        }

        @Test
        public void shouldListByProduct() throws Exception {
            assertThat(productRepository.count(), is(equalTo(2L)));
            assertThat(sceneRepository.count(), is(equalTo(20L)));

            Product product = products.get("Product-0");

            mockMvc.perform(get(Constants.ADMIN_PREFIX + "/products/{id}/scenes", product.getId())
                    .with(jwtBearerToken(admin, objectMapper)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(10)));

            assertThat(productRepository.count(), is(equalTo(2L)));
            assertThat(sceneRepository.count(), is(equalTo(20L)));
        }

        @Test
        public void shouldHandleNFE() throws Exception {
            assertThat(productRepository.count(), is(equalTo(2L)));
            assertThat(sceneRepository.count(), is(equalTo(20L)));

            Product product = products.get("Product-1");

            mockMvc.perform(get(Constants.ADMIN_PREFIX + "/products/{id}/scenes", product.getId() + 1)
                    .with(jwtBearerToken(admin, objectMapper)))
                    .andExpect(status().isNotFound());

            assertThat(productRepository.count(), is(equalTo(2L)));
            assertThat(sceneRepository.count(), is(equalTo(20L)));
        }
    }

    @Nested
    class Delete {
        @Test
        public void shouldWork() throws Exception {
            assertThat(sceneRepository.count(), is(equalTo(20L)));

            Product product = products.get("Product-0");
            Scene scene = productScenes.get(product.getName()).get(0);

            mockMvc.perform(delete(Constants.ADMIN_PREFIX + "/scenes/{id}", scene.getId())
                    .with(jwtBearerToken(admin, objectMapper)))
                    .andExpect(status().isOk());

            assertThat(sceneRepository.count(), is(equalTo(19L)));
        }

        @Test
        public void shouldHandleNotFound() throws Exception {
            assertThat(sceneRepository.count(), is(equalTo(20L)));

            mockMvc.perform(delete(Constants.ADMIN_PREFIX + "/scenes/{id}", maxSceneId + 1)
                    .with(jwtBearerToken(admin, objectMapper)))
                    .andExpect(status().isNotFound());

            assertThat(sceneRepository.count(), is(equalTo(20L)));
        }
    }

    @Nested
    class DeleteProductScenes {
        @Nested
        class WhenProductHasNoScenes {
            private Product emptyProduct;

            @BeforeEach
            public void beforeEach() {
                emptyProduct = productRepository.save(SceneTestHelper.productBuilder().build());
            }

            @Test
            public void shouldBeNoOpIfThereAreNoScenes() throws Exception {
                assertThat(productRepository.count(), is(equalTo(3L)));
                assertThat(sceneRepository.count(), is(equalTo(20L)));

                mockMvc.perform(delete(Constants.ADMIN_PREFIX + "/products/{id}/scenes", emptyProduct.getId())
                        .with(jwtBearerToken(admin, objectMapper)))
                        .andExpect(status().isOk());

                assertThat(productRepository.count(), is(equalTo(3L)));
                assertThat(sceneRepository.count(), is(equalTo(20L)));
            }

            @Test
            public void shouldHandleNotFound() throws Exception {
                assertThat(productRepository.count(), is(equalTo(3L)));
                assertThat(sceneRepository.count(), is(equalTo(20L)));

                mockMvc.perform(delete(Constants.ADMIN_PREFIX + "/products/{id}/scenes", emptyProduct.getId() + 1)
                        .with(jwtBearerToken(admin, objectMapper)))
                        .andExpect(status().isNotFound());

                assertThat(productRepository.count(), is(equalTo(3L)));
                assertThat(sceneRepository.count(), is(equalTo(20L)));
            }
        }

        @Nested
        class WhenThereAreScenes {
            private Product populatedProduct;

            @BeforeEach
            public void beforeEach() {
                populatedProduct = products.get("Product-1");
            }

            @Test
            public void shouldRemoveAllScenes() throws Exception {
                assertThat(productRepository.count(), is(equalTo(2L)));
                assertThat(sceneRepository.count(), is(equalTo(20L)));

                mockMvc.perform(delete(Constants.ADMIN_PREFIX + "/products/{id}/scenes", populatedProduct.getId())
                        .with(jwtBearerToken(admin, objectMapper)))
                        .andExpect(status().isOk());

                assertThat(productRepository.count(), is(equalTo(2L)));
                assertThat(sceneRepository.count(), is(equalTo(10L)));
            }

            @Test
            public void shouldHandleNotFound() throws Exception {
                assertThat(productRepository.count(), is(equalTo(2L)));
                assertThat(sceneRepository.count(), is(equalTo(20L)));

                mockMvc.perform(delete(Constants.ADMIN_PREFIX + "/products/{id}/scenes", populatedProduct.getId() + 1)
                        .with(jwtBearerToken(admin, objectMapper)))
                        .andExpect(status().isNotFound());

                assertThat(productRepository.count(), is(equalTo(2L)));
                assertThat(sceneRepository.count(), is(equalTo(20L)));
            }
        }
    }
}
