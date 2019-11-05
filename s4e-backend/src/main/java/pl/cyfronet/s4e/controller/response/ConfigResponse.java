package pl.cyfronet.s4e.controller.response;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ConfigResponse {
    String geoserverUrl;
    String geoserverWorkspace;
    String recaptchaSiteKey;
}
