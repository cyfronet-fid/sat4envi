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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Geometry;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.TransformException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;
import pl.cyfronet.s4e.IntegrationTest;
import pl.cyfronet.s4e.TestDbHelper;
import pl.cyfronet.s4e.bean.Scene;
import pl.cyfronet.s4e.data.repository.SceneRepository;
import pl.cyfronet.s4e.util.GeometryUtil;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static pl.cyfronet.s4e.sync.SceneAcceptorTestHelper.SCENE_KEY;

@IntegrationTest
@TestPropertySource(properties = {
        "s3.bucket=scene-acceptor-test"
})
class SceneAcceptorIntegrationTest {
    @Autowired
    private SceneAcceptor sceneAcceptor;

    @Autowired
    private SceneRepository sceneRepository;

    @Autowired
    private SceneAcceptorTestHelper sceneAcceptorTestHelper;

    @Autowired
    private TestDbHelper testDbHelper;

    @Autowired
    private GeometryUtil geometryUtil;

    @BeforeEach
    public void beforeEach() {
        testDbHelper.clean();
    }

    @Test
    public void test() throws FactoryException, TransformException {
        Long productId = sceneAcceptorTestHelper.setUpProduct();

        assertThat(sceneRepository.count(), is(equalTo(0L)));

        sceneAcceptor.accept(SCENE_KEY);

        await().until(() -> sceneRepository.findAllByProductId(productId), hasSize(greaterThan(0)));

        Scene scene = sceneRepository.findBySceneKey(SCENE_KEY).get();
        Geometry footprint3857 = scene.getFootprint();
        assertThat(footprint3857.toText(), is(equalTo("POLYGON ((2621616.6435465664 7481577.552185162, 2163180.3826032346 7564446.906756013, 2209931.229151686 7867684.010797335, 2686569.228715135 7780420.14854482, 2621616.6435465664 7481577.552185162))")));
        Geometry footprint4326 = geometryUtil.transform(footprint3857, "EPSG:3857", "EPSG:4326");
        assertThat(footprint4326.toText(), is(equalTo("POLYGON ((55.612087 23.550383, 56.030285000000006 19.43218, 57.522560000000006 19.85215, 57.099194 24.133862, 55.612087 23.550383))")));
    }
}
