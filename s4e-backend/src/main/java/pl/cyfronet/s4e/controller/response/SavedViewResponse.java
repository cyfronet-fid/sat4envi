package pl.cyfronet.s4e.controller.response;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.beans.factory.annotation.Value;

import java.time.ZonedDateTime;
import java.util.Map;

public interface SavedViewResponse {
    @Value("#{target.id}")
    @Schema(format = "uuid")
    String getUuid();

    String getCaption();

    @Value("#{@timeHelper.getZonedDateTimeWithDefaultZone(target.createdAt)}")
    ZonedDateTime getCreatedAt();

    @Schema(description = "a path to the thumbnail")
    String getThumbnail();

    @Schema(description = "a map (key-value) describing the state of the map view")
    Map<String, Object> getConfiguration();
}
