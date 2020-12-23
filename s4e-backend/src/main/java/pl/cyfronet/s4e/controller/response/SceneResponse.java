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

package pl.cyfronet.s4e.controller.response;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Builder;
import lombok.Data;
import org.locationtech.jts.geom.Geometry;
import pl.cyfronet.s4e.bean.Legend;
import pl.cyfronet.s4e.data.repository.projection.ProjectionWithId;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Set;

@Data
@Builder
public class SceneResponse {
    public interface Projection extends ProjectionWithId {
        ProjectionWithId getProduct();
        String getSceneKey();
        LocalDateTime getTimestamp();
        Geometry getFootprint();
        Legend getLegend();
        JsonNode getSceneContent();
        JsonNode getMetadataContent();
    }

    private Long id;
    private Long productId;
    private String sceneKey;
    private ZonedDateTime timestamp;
    private String footprint;
    private Legend legend;
    private Set<String> artifacts;
    private JsonNode metadataContent;
}
