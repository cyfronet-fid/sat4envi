package pl.cyfronet.s4e.controller.response;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Builder;
import lombok.Data;
import org.locationtech.jts.geom.Geometry;
import pl.cyfronet.s4e.bean.Legend;
import pl.cyfronet.s4e.data.repository.projection.ProjectionWithId;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Set;

@Data
@Builder
public class SceneResponse {
    public interface Projection extends ProjectionWithId {
        ProjectionWithId getProduct();
        String getSceneKey();
        LocalDateTime getTimestamp();
        Geometry getFootprint();
        Legend getLegend();
        JsonNode getSceneContent();
        JsonNode getMetadataContent();
    }

    private Long id;
    private Long productId;
    private String sceneKey;
    private ZonedDateTime timestamp;
    private String footprint;
    private Legend legend;
    private Set<String> artifacts;
    private JsonNode metadataContent;
}
