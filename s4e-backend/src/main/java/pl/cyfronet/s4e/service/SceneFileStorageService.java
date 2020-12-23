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

package pl.cyfronet.s4e.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.cyfronet.s4e.data.repository.SceneRepository;
import pl.cyfronet.s4e.ex.NotFoundException;

import java.util.Map;

import static pl.cyfronet.s4e.bean.Schema.SCENE_SCHEMA_ARTIFACTS_KEY;

@Service
@RequiredArgsConstructor
public class SceneFileStorageService {

    private final SceneRepository sceneRepository;
    private final ObjectMapper objectMapper;

    interface SceneProjection {
        JsonNode getSceneContent();
    }

    public Map<String, String> getSceneArtifacts(Long id) throws NotFoundException {
        SceneProjection sceneProjection = sceneRepository.findById(id, SceneProjection.class)
                .orElseThrow(() -> new NotFoundException("Scene with id '" + id + "' not found"));
        return  objectMapper.convertValue(
                sceneProjection.getSceneContent().get(SCENE_SCHEMA_ARTIFACTS_KEY),
                new TypeReference<>() {});
    }
}
