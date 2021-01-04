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

package pl.cyfronet.s4e.sync;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pl.cyfronet.s4e.SchemaTestHelper;
import pl.cyfronet.s4e.TestResourceHelper;
import pl.cyfronet.s4e.bean.Product;
import pl.cyfronet.s4e.bean.Schema;
import pl.cyfronet.s4e.data.repository.ProductRepository;
import pl.cyfronet.s4e.data.repository.SchemaRepository;

import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
class SceneAcceptorTestHelper {
    public static final String SCENE_KEY = "Sentinel-1/GRDH/2020-02-28/S1A_IW_GRDH_1SDV_20200228T045117_20200228T045142_031448_039EDF_82C8.scene";

    private final SchemaRepository schemaRepository;
    private final ProductRepository productRepository;
    private final TestResourceHelper testResourceHelper;

    public Long setUpProduct() {
        Map<String, Schema> schemas = SchemaTestHelper.SCENE_AND_METADATA_SCHEMA_NAMES.stream()
                .map(path -> SchemaTestHelper.schemaBuilder(path, testResourceHelper).build())
                .map(schemaRepository::save)
                .collect(Collectors.toMap(Schema::getName, s -> s));

        return productRepository.save(Product.builder()
                .name("Sentinel-1-GRDH")
                .displayName("Sentinel-1-GRDH")
                .authorizedOnly(false)
                .accessType(Product.AccessType.OPEN)
                .layerName("sentinel_1_grdh")
                .sceneSchema(schemas.get("Sentinel-1.scene.v1.json"))
                .metadataSchema(schemas.get("Sentinel-1.metadata.v1.json"))
                .granuleArtifactRule(Map.of("default", "quicklook", "COG", "RGBs_8b"))
                .build()).getId();
    }
}
