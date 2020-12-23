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
import lombok.Data;
import pl.cyfronet.s4e.bean.Legend;

import java.time.LocalDateTime;

@Data
class AdminSceneResponse {
    @Data
    public static class ProductPart {
        private Long id;
        private String name;
    }

    @Data
    public static class FootprintPart {
        private String epsg3857;
        private String epsg4326;
    }

    private Long id;
    private ProductPart product;
    private String sceneKey;
    private LocalDateTime timestamp;
    private FootprintPart footprint;
    private Legend legend;
    private JsonNode sceneContent;
    private JsonNode metadataContent;
}
