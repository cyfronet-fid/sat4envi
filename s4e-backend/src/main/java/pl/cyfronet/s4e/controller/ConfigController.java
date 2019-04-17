package pl.cyfronet.s4e.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.cyfronet.s4e.Constants;
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

    @GetMapping("/config")
    public ConfigResponse config() {
        return ConfigResponse.builder()
                .geoserverUrl(geoserverOutsideBaseUrl)
                .geoserverWorkspace(geoserverWorkspace)
                .backendDateFormat(Constants.JACKSON_DATE_FORMAT)
                .build();
    }
}
