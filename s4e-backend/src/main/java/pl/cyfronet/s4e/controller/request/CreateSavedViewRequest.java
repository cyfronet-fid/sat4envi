package pl.cyfronet.s4e.controller.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.util.Map;

@Data
@Builder
public class CreateSavedViewRequest {
    @NotEmpty
    @Schema(required = true, example = "Great SavedView")
    private String caption;

    @NotEmpty
    @Schema(required = true, format = "base64", description = "FIXME: not implemented")
    private String thumbnail;

    @NotEmpty
    @Schema(required = true, description = "a map (key-value) describing the state of the map view")
    private Map<String, Object> configuration;
}
