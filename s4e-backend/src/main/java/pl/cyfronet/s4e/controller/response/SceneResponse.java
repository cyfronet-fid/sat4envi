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
        LocalDateTime getTimestamp();
        Legend getLegend();
        JsonNode getSceneContent();
    }

    private Long id;
    private Long productId;
    private ZonedDateTime timestamp;
    private Legend legend;
    private Set<String> artifactNames;

    public static SceneResponse of(Long productId, Projection scene, Function<LocalDateTime, ZonedDateTime> timestampConverter) {
        return SceneResponse.builder()
                .id(scene.getId())
                .productId(productId)
                .timestamp(timestampConverter.apply(scene.getTimestamp()))
                .legend(scene.getLegend())
                .artifactNames(getArtifactNames(scene))
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
