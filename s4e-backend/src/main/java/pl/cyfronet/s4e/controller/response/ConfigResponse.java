package pl.cyfronet.s4e.controller.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ConfigResponse {
    @Schema(example = "/wms")
    String geoserverUrl;
    @Schema(example = "development")
    String geoserverWorkspace;
    @Schema(example = "6LeIxAcTAAAAAJcZVRqyHh71UMIEGNQ_MXjiZKhI")
    String recaptchaSiteKey;
}
