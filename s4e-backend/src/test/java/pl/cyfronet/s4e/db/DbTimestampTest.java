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

package pl.cyfronet.s4e.db;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import pl.cyfronet.s4e.BasicTest;
import pl.cyfronet.s4e.data.repository.ProductRepository;
import pl.cyfronet.s4e.data.repository.SceneRepository;

import java.time.LocalDateTime;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static pl.cyfronet.s4e.SceneTestHelper.productBuilder;
import static pl.cyfronet.s4e.SceneTestHelper.sceneBuilder;

@BasicTest
public class DbTimestampTest {
    @Autowired
    private SceneRepository sceneRepository;

    @Autowired
    private ProductRepository productRepository;

    @BeforeEach
    public void beforeEach() {
        sceneRepository.deleteAll();
        productRepository.deleteAll();
    }

    @Test
    public void shouldSaveTimestampWithoutDSTCorrections() {
        val product = productRepository.save(productBuilder().build());

        // A datetime, which if Polish timezone is used gets shifted one hour forward
        // on writing to DB.
        LocalDateTime dstBorderTimestamp = LocalDateTime.of(2019, 3, 31, 2, 0);

        val scene = sceneRepository.save(sceneBuilder(product, dstBorderTimestamp).build());

        val sceneId = sceneRepository.save(scene).getId();

        val retrievedScene = sceneRepository.findById(sceneId).get();
        assertThat(retrievedScene.getTimestamp(), is(equalTo(dstBorderTimestamp)));
    }
}
