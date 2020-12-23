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

package pl.cyfronet.s4e.util;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import pl.cyfronet.s4e.data.repository.SceneRepository;
import pl.cyfronet.s4e.ex.NotFoundException;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.function.Supplier;

import static pl.cyfronet.s4e.bean.Schema.SCENE_SCHEMA_ARTIFACTS_KEY;

@Service
@RequiredArgsConstructor
@Validated
public class SceneArtifactsHelper {
    private final SceneRepository sceneRepository;

    interface SceneProjection {
        Long getId();
        JsonNode getSceneContent();
    }

    public String getArtifact(@NotNull Long sceneId, @NotBlank String artifactName) throws NotFoundException {
        val sceneProjection = sceneRepository.findById(sceneId, SceneProjection.class)
                .orElseThrow(constructNFE(sceneId));
        try {
            return sceneProjection.getSceneContent().get(SCENE_SCHEMA_ARTIFACTS_KEY).get(artifactName).asText().substring(1);
        } catch (NullPointerException e) {
            throw new NotFoundException("Artifact with name '" + artifactName + "' for Scene with id '" + sceneId + "' not found");
        }
    }

    private Supplier<NotFoundException> constructNFE(Long id) {
        return () -> new NotFoundException("Scene with id '" + id + "' not found");
    }
}
