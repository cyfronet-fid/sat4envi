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

package pl.cyfronet.s4e.sync.step;

import jakarta.json.JsonObject;
import jakarta.json.JsonString;
import lombok.Builder;
import lombok.val;
import pl.cyfronet.s4e.ex.S3ClientException;
import pl.cyfronet.s4e.service.SceneStorage;
import pl.cyfronet.s4e.sync.Error;
import pl.cyfronet.s4e.sync.context.BaseContext;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static pl.cyfronet.s4e.bean.Schema.SCENE_SCHEMA_ARTIFACTS_KEY;
import static pl.cyfronet.s4e.sync.Error.*;

@Builder
public class VerifyAllArtifactsExist<T extends BaseContext> implements Step<T, Error> {
    private final Supplier<SceneStorage> sceneStorage;

    private final Function<T, JsonObject> sceneJson;
    private final BiConsumer<T, Map<String, String>> update;

    @Override
    public Error apply(T context) {
        val error = context.getError();
        SceneStorage sceneStorage = this.sceneStorage.get();

        JsonObject sceneJson = this.sceneJson.apply(context);

        val artifacts = new HashMap<String, String>();

        JsonObject artifactsObject = sceneJson.getJsonObject(SCENE_SCHEMA_ARTIFACTS_KEY);
        Map<String, String> notFound = new LinkedHashMap<>();
        for (val entry : artifactsObject.entrySet()) {
            String name = entry.getKey();
            String path = ((JsonString) entry.getValue()).getString();
            if (!isPathCorrect(path)) {
                return error.code(ERR_ARTIFACT_PATH_INCORRECT)
                        .parameter("artifact_" + name, path).build();
            }
            String artifactKey = pathToKey(path);
            artifacts.put(name, artifactKey);
            try {
                if (!sceneStorage.exists(artifactKey)) {
                    notFound.put(name, artifactKey);
                }
            } catch (S3ClientException e) {
                return error.code(ERR_S3_CLIENT_EXCEPTION).cause(e)
                        .parameter("artifact_" + name, artifactKey).build();
            }
        }
        if (!notFound.isEmpty()) {
            notFound.forEach((k,v) -> error.parameter("artifact_" + k, v));
            return error.code(ERR_ARTIFACTS_NOT_FOUND).build();
        }

        update.accept(context, artifacts);

        return null;
    }

    private boolean isPathCorrect(String path) {
        return path.startsWith("/");
    }

    private String pathToKey(String path) {
        return path.substring(1);
    }
}
