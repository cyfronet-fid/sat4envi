package pl.cyfronet.s4e.controller.response;

import lombok.Builder;
import lombok.Data;
import pl.cyfronet.s4e.bean.Scene;
import pl.cyfronet.s4e.util.TimeHelper;

import java.time.ZoneId;
import java.time.ZonedDateTime;

@Data
@Builder
public class SearchResponse {
    private Long id;
    private Long productId;
    private ZonedDateTime timestamp;

    public static SearchResponse of(Scene scene, ZoneId zoneId, TimeHelper timeHelper) {
        return SearchResponse.builder()
                .id(scene.getId())
                .productId(scene.getProduct().getId())
                .timestamp(timeHelper.getZonedDateTime(scene.getTimestamp(), zoneId))
                .build();
    }
}
