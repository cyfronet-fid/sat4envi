package pl.cyfronet.s4e.controller.response;

import lombok.Builder;
import lombok.Data;
import pl.cyfronet.s4e.bean.Legend;
import pl.cyfronet.s4e.bean.Scene;
import pl.cyfronet.s4e.util.TimeHelper;

import java.time.ZoneId;
import java.time.ZonedDateTime;

@Data
@Builder
public class SceneResponse {
    private Long id;
    private Long productId;
    private ZonedDateTime timestamp;
    private Legend legend;

    public static SceneResponse of(Scene scene, ZoneId zoneId, TimeHelper timeHelper) {
        return SceneResponse.builder()
                .id(scene.getId())
                .productId(scene.getProduct().getId())
                .timestamp(timeHelper.getZonedDateTime(scene.getTimestamp(), zoneId))
                .legend(scene.getLegend())
                .build();
    }
}
