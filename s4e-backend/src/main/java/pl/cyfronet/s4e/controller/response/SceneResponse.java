package pl.cyfronet.s4e.controller.response;

import lombok.Builder;
import lombok.Data;
import pl.cyfronet.s4e.bean.Legend;
import pl.cyfronet.s4e.data.repository.projection.ProjectionWithId;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.function.Function;

@Data
@Builder
public class SceneResponse {
    public interface Projection extends ProjectionWithId {
        LocalDateTime getTimestamp();
        Legend getLegend();
    }

    private Long id;
    private Long productId;
    private ZonedDateTime timestamp;
    private Legend legend;

    public static SceneResponse of(Long productId, Projection scene, Function<LocalDateTime, ZonedDateTime> timestampConverter) {
        return SceneResponse.builder()
                .id(scene.getId())
                .productId(productId)
                .timestamp(timestampConverter.apply(scene.getTimestamp()))
                .legend(scene.getLegend())
                .build();
    }
}
