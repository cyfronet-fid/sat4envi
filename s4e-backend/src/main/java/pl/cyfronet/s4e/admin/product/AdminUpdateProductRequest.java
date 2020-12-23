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

package pl.cyfronet.s4e.admin.product;

import lombok.Builder;
import lombok.Data;
import pl.cyfronet.s4e.bean.Legend;
import pl.cyfronet.s4e.bean.Product;

import javax.validation.constraints.Pattern;
import java.util.Map;

@Data
@Builder
class AdminUpdateProductRequest {
    @Pattern(regexp = "[-_a-zA-Z0-9]+")
    private String name;

    private String displayName;

    private String description;

    private Product.AccessType accessType;

    private Legend legend;

    @Pattern(regexp = "[_a-z0-9]+")
    private String layerName;

    private String sceneSchemaName;

    private String metadataSchemaName;

    private Map<String, String> granuleArtifactRule;

    private String productCategoryName;
}
