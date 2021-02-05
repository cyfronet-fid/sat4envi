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
import lombok.val;
import org.leadpony.justify.api.JsonValidationService;
import org.springframework.stereotype.Component;
import pl.cyfronet.s4e.bean.Schema;
import pl.cyfronet.s4e.data.repository.ProductRepository;
import pl.cyfronet.s4e.data.repository.SchemaRepository;
import pl.cyfronet.s4e.service.SceneStorage;
import pl.cyfronet.s4e.sync.context.BaseContext;
import pl.cyfronet.s4e.sync.context.Context;
import pl.cyfronet.s4e.sync.step.*;
import pl.cyfronet.s4e.sync.step.metadata.*;
import pl.cyfronet.s4e.util.GeometryUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Component
@RequiredArgsConstructor
public class PipelineFactory {
    public static final String SCENE_METADATA_ARTIFACT = "metadata";

    private final SceneStorage sceneStorage;
    private final SchemaRepository schemaRepository;
    private final ProductRepository productRepository;
    private final GeometryUtil geometryUtil;
    private final ScenePersister scenePersister;
    private final JsonValidationService jsonValidationService = JsonValidationService.newInstance();

    public List<Step<Context, Error>> build() {
        val pipeline = new ArrayList<Step<Context, Error>>();
        loadAndVerifySceneFile(pipeline);
        copyMetadataKeyToMetadataContext(pipeline);
        loadAndVerifyMetadataFile(pipeline);
        ingestMetadata(pipeline);
        persistPrototype(pipeline);
        return pipeline;
    }

    private void loadAndVerifySceneFile(List<Step<Context, Error>> pipeline) {
        errorContext(pipeline, c -> Map.of(
                "file_type", "scene",
                "file_key", c.getScene().getKey())
        );
        append(pipeline, List.of(
                // Check scene file exists.
                LoadKeyContent.<Context>builder()
                        .sceneStorage(() -> sceneStorage)
                        .key(c -> c.getScene().getKey())
                        .update((c, content) -> c.getScene().setContent(content))
                        .build(),
                // Check scene schema exists and is of correct type.
                LoadSchemaOfCorrectType.<Context>builder()
                        .schemaRepository(() -> schemaRepository)
                        .jsonValidationService(() -> jsonValidationService)
                        .content(c -> c.getScene().getContent())
                        .requiredType(c -> Schema.Type.SCENE)
                        .update((c, schemaData) -> c.getScene().setSchema(schemaData))
                        .build(),
                // Validate scene schema.
                LoadValidatedJson.<Context>builder()
                        .jsonValidationService(() -> jsonValidationService)
                        .content(c -> c.getScene().getContent())
                        .jsonSchema(c -> c.getScene().getSchema().getJsonSchema())
                        .update((c, jsonObject) -> c.getScene().setJson(jsonObject))
                        .build(),
                // Check product_type exists.
                LoadProduct.<Context>builder()
                        .productRepository(() -> productRepository)
                        .json(c -> c.getScene().getJson())
                        .update(Context::setProduct)
                        .build(),
                // Check product sceneSchema matches scene file schema.
                VerifySchemasMatch.<Context>builder()
                        .fileSchemaName(c -> c.getScene().getSchema().getName())
                        .productSchemaName(c -> c.getProduct().getSceneSchema().getName())
                        .build(),
                // Check all artifacts exist.
                VerifyAllArtifactsExist.<Context>builder()
                        .sceneStorage(() -> sceneStorage)
                        .sceneJson(c -> c.getScene().getJson())
                        .update((c, artifacts) -> c.getScene().setArtifacts(artifacts))
                        .build()
        ));
    }

    private void copyMetadataKeyToMetadataContext(List<Step<Context, Error>> pipeline) {
        append(pipeline, (Context c) -> {
            String value = c.getScene().getArtifacts().get(SCENE_METADATA_ARTIFACT);
            c.getMetadata().setKey(value);
            return null;
        });
    }

    private void loadAndVerifyMetadataFile(List<Step<Context, Error>> pipeline) {
        errorContext(pipeline, c -> Map.of(
                "file_type", "metadata",
                "file_key", c.getMetadata().getKey())
        );
        append(pipeline, List.of(
                // Check metadata file exists.
                LoadKeyContent.<Context>builder()
                        .sceneStorage(() -> sceneStorage)
                        .key(c -> c.getMetadata().getKey())
                        .update((c, content) -> c.getMetadata().setContent(content))
                        .build(),
                // Check metadata schema exists and is of correct type.
                LoadSchemaOfCorrectType.<Context>builder()
                        .schemaRepository(() -> schemaRepository)
                        .jsonValidationService(() -> jsonValidationService)
                        .content(c -> c.getMetadata().getContent())
                        .requiredType(c -> Schema.Type.METADATA)
                        .update((c, schemaData) -> c.getMetadata().setSchema(schemaData))
                        .build(),
                // Validate metadata schema.
                LoadValidatedJson.<Context>builder()
                        .jsonValidationService(() -> jsonValidationService)
                        .content(c -> c.getMetadata().getContent())
                        .jsonSchema(c -> c.getMetadata().getSchema().getJsonSchema())
                        .update((c, jsonObject) -> c.getMetadata().setJson(jsonObject))
                        .build(),
                // Check product metadataSchema matches metadata file schema.
                VerifySchemasMatch.<Context>builder()
                        .fileSchemaName(c -> c.getMetadata().getSchema().getName())
                        .productSchemaName(c -> c.getProduct().getMetadataSchema().getName())
                        .build()
        ));
    }

    private void ingestMetadata(List<Step<Context, Error>> pipeline) {
        errorContext(pipeline, c -> Map.of(
                "file_type", "metadata",
                "file_key", c.getMetadata().getKey(),
                "parsing_phase", "init")
        );
        append(pipeline, Init.builder()
                .update(Context::setPrototype)
                .build());

        errorContext(pipeline, c -> Map.of(
                "file_type", "metadata",
                "file_key", c.getMetadata().getKey(),
                "parsing_phase", "footprint")
        );
        append(pipeline, IngestFootprint.<Context>builder()
                .geometryUtil(() -> geometryUtil)
                .metadataJson(c -> c.getMetadata().getJson())
                .update((c, footprint) -> c.getPrototype().footprint(footprint))
                .build());
    }

    private void persistPrototype(List<Step<Context, Error>> pipeline) {
        errorContext(pipeline, c -> Map.of(
                "phase", "persist")
        );
        append(pipeline, Persist.<Context>builder()
                .scenePersister(() -> scenePersister)
                .prototype(c -> c.getPrototype().build())
                .updateSceneId(Context::setSceneId)
                .build()
        );
    }

    private <T extends BaseContext> void errorContext(List<Step<T, Error>> pipeline, Function<T, Map<String, String>> parameters) {
        pipeline.add(ResetErrorParameters.<T>builder()
                .parameters(parameters)
                .build());
    }

    private <T extends BaseContext> void append(List<Step<T, Error>> pipeline, List<Step<T, Error>> steps) {
        pipeline.addAll(steps);
    }

    private <T extends BaseContext> void append(List<Step<T, Error>> pipeline, Step<T, Error> step) {
        append(pipeline, List.of(step));
    }
}
