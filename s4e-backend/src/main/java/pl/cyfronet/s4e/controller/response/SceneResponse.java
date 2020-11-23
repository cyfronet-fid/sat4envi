package pl.cyfronet.s4e.controller.response;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Builder;
import lombok.Data;
import pl.cyfronet.s4e.bean.Legend;
import pl.cyfronet.s4e.data.repository.projection.ProjectionWithId;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Iterator;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static pl.cyfronet.s4e.bean.Schema.SCENE_SCHEMA_ARTIFACTS_KEY;

@Data
@Builder
public class SceneResponse {
    public interface Projection extends ProjectionWithId {
        String getSceneKey();
        LocalDateTime getTimestamp();
        Legend getLegend();
        JsonNode getSceneContent();
        JsonNode getMetadataContent();
    }

    private Long id;
    private Long productId;
    private String sceneKey;
    private Legend legend;
    private Set<String> artifacts;
    private JsonNode metadataContent;
    private ZonedDateTime timestamp;

    public static SceneResponse of(Long productId, Projection scene, Function<LocalDateTime, ZonedDateTime> timestampConverter) {
        return SceneResponse.builder()
                .id(scene.getId())
                .productId(productId)
                .sceneKey(scene.getSceneKey())
                .timestamp(timestampConverter.apply(scene.getTimestamp()))
                .legend(scene.getLegend())
                .artifacts(getArtifactNames(scene))
                .metadataContent(scene.getMetadataContent())
                .build();
    }

    private static Set<String> getArtifactNames(Projection scene) {
        ObjectNode artifacts = (ObjectNode) scene.getSceneContent().get(SCENE_SCHEMA_ARTIFACTS_KEY);
        Iterator<String> artifactNamesIterator = artifacts.fieldNames();
        return Stream.generate(() -> null)
                .takeWhile(ignored -> artifactNamesIterator.hasNext())
                .map(ignored -> artifactNamesIterator.next())
                .collect(Collectors.toUnmodifiableSet());
    }
}
