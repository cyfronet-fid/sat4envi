package pl.cyfronet.s4e.controller.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import pl.cyfronet.s4e.controller.validation.Base64;
import pl.cyfronet.s4e.controller.validation.ContentType;
import pl.cyfronet.s4e.controller.validation.ImageDimensions;

import javax.validation.constraints.NotEmpty;
import java.util.Map;

@Data
@Builder
public class CreateSavedViewRequest {
    @NotEmpty
    @Schema(required = true, example = "Great SavedView")
    private String caption;

    @NotEmpty
    @Base64
    @ContentType(pattern = "image/(jpeg|png|gif)")
    @ImageDimensions(maxWidth = 500, maxHeight = 500)
    @Schema(required = true, format = "base64")
    private String thumbnail;

    @NotEmpty
    @Schema(required = true, description = "a map (key-value) describing the state of the map view")
    private Map<String, Object> configuration;
}
