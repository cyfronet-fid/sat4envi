package pl.cyfronet.s4e.controller;

import io.swagger.annotations.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.cyfronet.s4e.controller.response.ConfigResponse;

import static pl.cyfronet.s4e.Constants.API_PREFIX_V1;

@RestController
@RequestMapping(API_PREFIX_V1)
@RequiredArgsConstructor
public class ConfigController {
    @Value("${geoserver.outsideBaseUrl}")
    private String geoserverOutsideBaseUrl;

    @Value("${geoserver.workspace}")
    private String geoserverWorkspace;

    @Value("${recaptcha.validation.siteKey}")
    private String recaptchaSiteKey;

    @ApiOperation("Get the front-end app configuration")
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK", examples = @Example({
                    @ExampleProperty(mediaType = "application/json", value = "{\n" +
                            "    \"geoserverUrl\": \"http://localhost:8080/geoserver/rest\",\n" +
                            "    \"geoserverWorkspace\": \"development\",\n" +
                            "    \"recaptchaSiteKey\": \"6LeIxAcTAAAAAJcZVRqyHh71UMIEGNQ_MXjiZKhI\"\n" +
                            "}")
            }))
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
