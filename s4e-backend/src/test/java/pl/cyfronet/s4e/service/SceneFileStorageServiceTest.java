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

package pl.cyfronet.s4e.service;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import pl.cyfronet.s4e.BasicTest;
import pl.cyfronet.s4e.SceneTestHelper;
import pl.cyfronet.s4e.TestDbHelper;
import pl.cyfronet.s4e.bean.Product;
import pl.cyfronet.s4e.bean.Scene;
import pl.cyfronet.s4e.data.repository.ProductRepository;
import pl.cyfronet.s4e.data.repository.SceneRepository;
import pl.cyfronet.s4e.ex.NotFoundException;

import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.aMapWithSize;
import static pl.cyfronet.s4e.SceneTestHelper.productBuilder;

@BasicTest
@Slf4j
public class SceneFileStorageServiceTest {
    @Autowired
    private SceneRepository sceneRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private TestDbHelper testDbHelper;

    @Autowired
    private SceneFileStorageService sceneFileStorageService;

    private Scene scene;

    @BeforeEach
    public void beforeEach() {
        testDbHelper.clean();
        Product product = productRepository.save(productBuilder().build());
        //addscenewithmetadata
        scene = buildScene(product);
        sceneRepository.save(scene);
    }

    @AfterEach
    public void afterEach() {
        testDbHelper.clean();
    }

    private Scene buildScene(Product product) {
        return SceneTestHelper.sceneBuilder(product).build();
    }

    @Test
    public void shouldReturnSceneArtifacts() throws NotFoundException {
        Map<String, String> result = sceneFileStorageService.getSceneArtifacts(scene.getId());

        assertThat(result, aMapWithSize(2));
    }
}
