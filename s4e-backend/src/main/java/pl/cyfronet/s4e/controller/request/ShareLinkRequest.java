package pl.cyfronet.s4e.controller.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import pl.cyfronet.s4e.controller.validation.Base64;
import pl.cyfronet.s4e.controller.validation.ContentType;
import pl.cyfronet.s4e.controller.validation.ImageDimensions;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.util.List;

@Data
@Builder
public class ShareLinkRequest {
    @NotBlank
    private String caption;

    @NotBlank
    private String description;

    @NotEmpty
    @Base64
    @ContentType(pattern = "image/(jpeg|png|gif)")
    @ImageDimensions(maxWidth = 500, maxHeight = 500)
    @Schema(required = true, format = "base64")
    private String thumbnail;

    @NotBlank
    @Schema(required = true, description = "The link to share. Relative path with a leading slash", example = "/some/path?param=42")
    private String path;

    @NotEmpty
    private List<@Email String> emails;
}
