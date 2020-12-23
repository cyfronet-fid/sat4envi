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

package pl.cyfronet.s4e.sync;

import jakarta.json.Json;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import pl.cyfronet.s4e.BasicTest;
import pl.cyfronet.s4e.SceneTestHelper;
import pl.cyfronet.s4e.TestDbHelper;
import pl.cyfronet.s4e.TestGeometryHelper;
import pl.cyfronet.s4e.bean.Product;
import pl.cyfronet.s4e.bean.Scene;
import pl.cyfronet.s4e.data.repository.ProductRepository;
import pl.cyfronet.s4e.data.repository.SceneRepository;
import pl.cyfronet.s4e.ex.NotFoundException;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

@BasicTest
class ScenePersisterTest {
    @Autowired
    private ScenePersister scenePersister;

    @Autowired
    private SceneRepository sceneRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private TestDbHelper testDbHelper;

    @BeforeEach
    public void beforeEach() {
        testDbHelper.clean();
    }

    @Test
    public void shouldSaveNewScene() throws NotFoundException {
        Product product = createProduct();
        Prototype prototype = getPrototype(product.getId(), "key/path/of/a.scene");

        assertThat(listProductScenes(product.getId()), hasSize(0));

        Long persistedId = scenePersister.persist(prototype);

        assertThat(listProductScenes(product.getId()), hasItems(allOf(
                hasProperty("id", equalTo(persistedId)),
                hasProperty("sceneKey", equalTo(prototype.getSceneKey()))
        )));
    }

    @Test
    public void shouldUpdateExistingScene() throws NotFoundException {
        Product product = createProduct();
        String sceneKey = "key/path/of/a.scene";
        Scene scene = createScene(product, sceneKey);
        Prototype prototype = getPrototype(product.getId(), sceneKey);

        assertThat(listProductScenes(product.getId()), hasItems(allOf(
                hasProperty("id", equalTo(scene.getId())),
                hasProperty("sceneKey", equalTo(sceneKey))
        )));

        Long persistedId = scenePersister.persist(prototype);

        assertThat(persistedId, is(equalTo(scene.getId())));
        assertThat(listProductScenes(product.getId()), hasItems(allOf(
                hasProperty("id", equalTo(persistedId)),
                hasProperty("sceneKey", equalTo(sceneKey))
        )));
    }

    @Test
    public void shouldVerifyUpdatedSceneProductMatches() {
        Product product1 = createProduct();
        Product product2 = createProduct();
        String sceneKey = "key/path/of/a.scene";
        Scene scene = createScene(product1, sceneKey);
        Prototype prototype = getPrototype(product2.getId(), sceneKey);

        assertThat(listProductScenes(product1.getId()), hasItems(allOf(
                hasProperty("id", equalTo(scene.getId())),
                hasProperty("sceneKey", equalTo(sceneKey))
        )));
        assertThat(listProductScenes(product2.getId()), is(empty()));

        assertThrows(IllegalArgumentException.class, () -> scenePersister.persist(prototype));

        assertThat(listProductScenes(product1.getId()), hasItems(allOf(
                hasProperty("id", equalTo(scene.getId())),
                hasProperty("sceneKey", equalTo(sceneKey))
        )));
        assertThat(listProductScenes(product2.getId()), is(empty()));
    }

    private Product createProduct() {
        return productRepository.save(SceneTestHelper.productBuilder().build());
    }

    private Scene createScene(Product product, String sceneKey) {
        return sceneRepository.save(SceneTestHelper.sceneBuilder(product)
                .sceneKey(sceneKey)
                .build());
    }

    private Prototype getPrototype(Long productId, String sceneKey) {
        return Prototype.builder()
                .sceneKey(sceneKey)
                .productId(productId)
                .footprint(TestGeometryHelper.ANY_POLYGON)
                .sceneJson(Json.createObjectBuilder()
                        .add("artifacts", Json.createObjectBuilder()
                                .add("default_artifact", "some/value.tif")
                                .build())
                        .build())
                .metadataJson(Json.createObjectBuilder()
                        .add("format", "GeoTiff")
                        .add("sensing_time", "2020-01-01T00:00:00Z")
                        .build())
                .build();
    }

    private List<Scene> listProductScenes(Long productId) {
        return sceneRepository.findAllByProductId(productId);
    }

}
