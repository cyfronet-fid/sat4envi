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

import lombok.Builder;
import lombok.Data;
import pl.cyfronet.s4e.bean.Legend;
import pl.cyfronet.s4e.bean.Product;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.util.Map;

@Data
@Builder
class AdminCreateProductRequest {
    @NotEmpty
    @Pattern(regexp = "[-_a-zA-Z0-9]+")
    private String name;

    @NotEmpty
    private String displayName;

    @NotEmpty
    private String description;

    @NotNull
    private Boolean downloadOnly;

    @NotNull
    private Boolean authorizedOnly;

    @NotNull
    private Product.AccessType accessType;

    private Legend legend;

    @NotEmpty
    @Pattern(regexp = "[_a-z0-9]+")
    private String layerName;

    @NotEmpty
    private String sceneSchemaName;

    @NotEmpty
    private String metadataSchemaName;

    @NotEmpty
    private Map<String, String> granuleArtifactRule;

    @NotEmpty
    private String productCategoryName;

    @NotNull
    private Long rank;
}
