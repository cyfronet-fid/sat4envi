package pl.cyfronet.s4e.controller.response;

import lombok.Builder;
import lombok.Data;
import pl.cyfronet.s4e.Constants;
import pl.cyfronet.s4e.bean.Legend;
import pl.cyfronet.s4e.bean.Scene;

import java.time.ZonedDateTime;

@Data
@Builder
public class SceneResponse {
    private Long id;
    private Long productTypeId;
    private ZonedDateTime timestamp;
    private String layerName;
    private Legend legend;

    public static SceneResponse of(Scene scene) {
        return SceneResponse.builder()
                .id(scene.getId())
                .productTypeId(scene.getProductType().getId())
                .timestamp(ZonedDateTime.of(scene.getTimestamp(), Constants.ZONE_ID))
                .layerName(scene.getLayerName())
                .legend(scene.getLegend())
                .build();
    }
}
