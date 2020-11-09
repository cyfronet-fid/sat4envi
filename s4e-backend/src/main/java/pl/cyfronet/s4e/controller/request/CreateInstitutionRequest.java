package pl.cyfronet.s4e.controller.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import pl.cyfronet.s4e.controller.validation.Base64;
import pl.cyfronet.s4e.controller.validation.ContentType;
import pl.cyfronet.s4e.controller.validation.ImageDimensions;

import javax.validation.constraints.NotEmpty;

@Data
@Builder
public class CreateInstitutionRequest {
    @NotEmpty
    private String name;

    @Base64
    @ContentType(pattern = "image/(jpeg|png|gif)")
    @ImageDimensions(maxWidth = 500, maxHeight = 500)
    @Schema(required = true, format = "base64")
    private String emblem;

    private String address;

    private String postalCode;

    private String city;

    private String phone;

    private String secondaryPhone;

    private boolean zk;
}
