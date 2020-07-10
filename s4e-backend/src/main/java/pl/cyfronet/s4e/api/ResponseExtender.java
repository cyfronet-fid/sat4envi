package pl.cyfronet.s4e.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pl.cyfronet.s4e.controller.response.SearchResponse;
import pl.cyfronet.s4e.util.TimeHelper;

import java.time.ZoneId;
import java.util.Map;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class ResponseExtender {
    private final TimeHelper timeHelper;
    private final ObjectMapper objectMapper;

    public SearchResponse map(MappedScene scene, ZoneId zoneId) {
        return SearchResponse.builder()
                .id(scene.getId())
                .productId(scene.getProductId())
                .footprint(scene.getFootprint())
                .metadataContent(getMetadata(scene))
                .artifacts(getArtifacts(scene))
                .timestamp(timeHelper.getZonedDateTime(scene.getTimestamp(), zoneId))
                .build();
    }

    private Set<String> getArtifacts(MappedScene scene) {
        if (scene.getSceneContent() == null) {
            return null;
        }
        try {
            JsonNode artifacts = objectMapper.readTree(scene.getSceneContent()).get("artifacts");
            Map<String, String> treeMap = objectMapper.convertValue(artifacts, Map.class);
            return treeMap.keySet();
        } catch (JsonProcessingException e) {
            log.warn("Cannot parse scene content for id : " + scene.getId());
            return null;
        }

    }

    private JsonNode getMetadata(MappedScene scene) {
        if (scene.getMetadataContent() == null) {
            return null;
        }
        try {
            return objectMapper.readTree(scene.getMetadataContent());
        } catch (JsonProcessingException e) {
            log.warn("Cannot parse scene metadata for id : " + scene.getId());
            return null;
        }
    }
}