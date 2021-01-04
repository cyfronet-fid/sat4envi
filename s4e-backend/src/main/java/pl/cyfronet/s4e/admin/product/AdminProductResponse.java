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

package pl.cyfronet.s4e.admin.product;

import pl.cyfronet.s4e.bean.Legend;
import pl.cyfronet.s4e.controller.response.BasicProductCategoryResponse;

import java.util.Map;

interface AdminProductResponse {
    interface SceneSchema {
        Long getId();

        String getName();
    }

    interface MetadataSchema {
        Long getId();

        String getName();
    }

    Long getId();

    String getName();

    String getDisplayName();

    String getDescription();

    Boolean getAuthorizedOnly();

    String getAccessType();

    Legend getLegend();

    String getLayerName();

    SceneSchema getSceneSchema();

    MetadataSchema getMetadataSchema();

    Map<String, String> getGranuleArtifactRule();

    BasicProductCategoryResponse getProductCategory();
}
