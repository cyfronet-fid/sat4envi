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

package pl.cyfronet.s4e.util;

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

import javax.validation.ConstraintViolationException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

@BasicTest
class SceneArtifactsHelperTest {
    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private SceneRepository sceneRepository;

    @Autowired
    private SceneArtifactsHelper sceneArtifactsHelper;

    @Autowired
    private TestDbHelper testDbHelper;

    private Scene scene;

    @BeforeEach
    public void beforeEach() {
        testDbHelper.clean();

        Product product = productRepository.save(SceneTestHelper.productBuilder().build());
        scene = sceneRepository.save(SceneTestHelper.sceneBuilder(product).build());
    }

    @Test
    public void shouldWork() throws NotFoundException {
        assertThat(sceneArtifactsHelper.getArtifact(scene.getId(), "default_artifact"), is(equalTo("some/path")));
        assertThat(sceneArtifactsHelper.getArtifact(scene.getId(), "other_artifact"), is(equalTo("some/other/path")));
    }

    @Test
    public void shouldProhibitNullArguments() {
        assertThrows(ConstraintViolationException.class, () -> sceneArtifactsHelper.getArtifact(scene.getId(), null));
        assertThrows(ConstraintViolationException.class, () -> sceneArtifactsHelper.getArtifact(scene.getId(), " "));
        assertThrows(ConstraintViolationException.class, () -> sceneArtifactsHelper.getArtifact(null, "some_artifact"));
        assertThrows(ConstraintViolationException.class, () -> sceneArtifactsHelper.getArtifact(null, null));
        assertThrows(ConstraintViolationException.class, () -> sceneArtifactsHelper.getArtifact(null, " "));
    }

    @Test
    public void shouldThrowNFEIfSceneDoesntExist() {
        assertThrows(NotFoundException.class, () -> sceneArtifactsHelper.getArtifact(scene.getId() + 1, "other_artifact"));
    }

    @Test
    public void shouldThrowNFEForNonExistentArtifact() {
        assertThrows(NotFoundException.class, () -> sceneArtifactsHelper.getArtifact(scene.getId(), "not_existent"));
    }
}
