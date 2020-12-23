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

import com.fasterxml.jackson.databind.JsonNode;
import org.locationtech.jts.geom.Geometry;
import pl.cyfronet.s4e.bean.Legend;

import java.time.LocalDateTime;

public interface AdminSceneProjection {
    interface ProductProjection {
        Long getId();
        String getName();
    }

    Long getId();
    ProductProjection getProduct();
    String getSceneKey();
    LocalDateTime getTimestamp();
    Geometry getFootprint();
    Legend getLegend();
    JsonNode getSceneContent();
    JsonNode getMetadataContent();
}
