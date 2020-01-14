package pl.cyfronet.s4e.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.cyfronet.s4e.controller.response.ConfigResponse;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static pl.cyfronet.s4e.Constants.API_PREFIX_V1;

@RestController
@RequestMapping(path = API_PREFIX_V1, produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "config", description = "The Config API")
public class ConfigController {
    @Value("${geoserver.outsideBaseUrl}")
    private String geoserverOutsideBaseUrl;

    @Value("${geoserver.workspace}")
    private String geoserverWorkspace;

    @Value("${recaptcha.validation.siteKey}")
    private String recaptchaSiteKey;

    @Operation(summary = "Get the front-end app configuration")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK")
    })
    @GetMapping("/config")
    public ConfigResponse config() {
        return ConfigResponse.builder()
                .geoserverUrl(geoserverOutsideBaseUrl)
                .geoserverWorkspace(geoserverWorkspace)
                .recaptchaSiteKey(recaptchaSiteKey)
                .build();
    }
}
