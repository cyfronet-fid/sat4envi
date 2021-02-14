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

package pl.cyfronet.s4e.service;

import io.micrometer.core.instrument.Counter;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.stereotype.Service;
import pl.cyfronet.s4e.data.repository.projection.ProjectionWithId;

@Service
@RequiredArgsConstructor
public class MetricService {
    private interface ProductProjection extends ProjectionWithId {
        String getLayerName();
    }

    private interface SceneProjection extends ProjectionWithId {
        ProductProjection getProduct();
    }

    private final Counter sentinel1RequestCounter;
    private final Counter sentinel2RequestCounter;
    private final Counter sentinel3RequestCounter;
    private final Counter sentinel5PRequestCounter;
    private final SceneService sceneService;

    public void incrementCounter(Long sceneId) {
        val sceneProjection = sceneService.findById(sceneId, SceneProjection.class);
        if (sceneProjection.isPresent()) {
            String name = sceneProjection.get().getProduct().getLayerName();
            if (name.contains("sentinel_1")) {
                sentinel1RequestCounter.increment();
            }
            if (name.contains("sentinel_2")) {
                sentinel2RequestCounter.increment();
            }
            if (name.contains("sentinel_3")) {
                sentinel3RequestCounter.increment();
            }
            if (name.contains("sentinel_5p")) {
                sentinel5PRequestCounter.increment();
            }
        }
    }
}
