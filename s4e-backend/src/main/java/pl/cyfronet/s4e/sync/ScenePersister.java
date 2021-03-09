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

package pl.cyfronet.s4e.sync;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.json.JsonObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pl.cyfronet.s4e.bean.Product;
import pl.cyfronet.s4e.bean.Scene;
import pl.cyfronet.s4e.data.repository.ProductRepository;
import pl.cyfronet.s4e.data.repository.SceneRepository;
import pl.cyfronet.s4e.ex.NotFoundException;

import java.util.Locale;

@Component
@RequiredArgsConstructor
@Slf4j
public class ScenePersister {
    private final ProductRepository productRepository;
    private final SceneRepository sceneRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public Long persist(Prototype prototype) throws NotFoundException {
        Product product = productRepository.findById(prototype.getProductId())
                .orElseThrow(() -> new NotFoundException("Product with id '" + prototype.getProductId() + "' not found"));

        JsonNode sceneJsonNode = convert(prototype.getSceneJson());
        JsonNode metadataJsonNode = convert(prototype.getMetadataJson());

        Scene scene = sceneRepository.findBySceneKey(prototype.getSceneKey()).orElse(new Scene());
        if (scene.getProduct() != null && !product.equals(scene.getProduct())) {
            Long existingProductId = scene.getProduct().getId();
            Long prototypeProductId = prototype.getProductId();
            throw new IllegalArgumentException(String.format(Locale.ENGLISH, "Existing Scene's Product %d is not equal to the prototype Product %d", existingProductId, prototypeProductId));
        } else {
            scene.setProduct(product);
        }
        scene.setSceneKey(prototype.getSceneKey());
        scene.setFootprint(prototype.getFootprint());
        scene.setSceneContent(sceneJsonNode);
        scene.setMetadataContent(metadataJsonNode);

        if (scene.getId() == null) {
            scene = sceneRepository.save(scene);
        }

        return scene.getId();
    }

    private JsonNode convert(JsonObject sceneJson) {
        try {
            return objectMapper.readTree(sceneJson.toString());
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
