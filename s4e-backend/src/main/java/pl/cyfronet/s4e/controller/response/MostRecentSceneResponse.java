package pl.cyfronet.s4e.controller.response;

import lombok.Builder;
import lombok.Data;

import java.time.ZonedDateTime;

@Data
@Builder
public class MostRecentSceneResponse {
    private Long sceneId;
    private ZonedDateTime timestamp;
}
