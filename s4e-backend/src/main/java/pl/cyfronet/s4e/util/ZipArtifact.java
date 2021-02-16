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

package pl.cyfronet.s4e.util;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.val;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public class ZipArtifact {
    public static Optional<String> getName(JsonNode artifactsNode) {
        if (artifactsNode == null || !artifactsNode.isObject()) {
            return Optional.empty();
        }

        return getName(getArtifactsMap(artifactsNode));
    }

    public static Optional<String> getName(Map<String, String> artifacts) {
        if (artifacts == null || artifacts.isEmpty()) {
            return Optional.empty();
        }

        return artifacts
                .entrySet().stream()
                .filter(entry -> entry.getValue() != null)
                .filter(entry -> entry.getValue().endsWith(".zip"))
                .map(Map.Entry::getKey)
                .sorted()
                .findFirst();
    }

    private static Map<String, String> getArtifactsMap(JsonNode artifactsNode) {
        val map = new LinkedHashMap<String, String>();
        artifactsNode.fields().forEachRemaining(entry -> {
            val value = entry.getValue();
            if (value.isTextual()) {
                map.put(entry.getKey(), value.asText());
            }
        });
        return map;
    }
}
