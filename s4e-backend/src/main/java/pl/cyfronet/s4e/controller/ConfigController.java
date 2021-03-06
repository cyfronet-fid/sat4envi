/*
 * Copyright 2021 ACC Cyfronet AGH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

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
import pl.cyfronet.s4e.Constants;
import pl.cyfronet.s4e.controller.response.ConfigResponse;
import pl.cyfronet.s4e.kvconfig.KeyValueConfigSupplier;
import pl.cyfronet.s4e.properties.GeoServerProperties;
import pl.cyfronet.s4e.properties.OsmProperties;
import pl.cyfronet.s4e.search.SearchPanelConfigResponse;
import pl.cyfronet.s4e.search.SearchPanelConfigResponseSupplier;
import pl.cyfronet.s4e.security.AppUserDetails;
import pl.cyfronet.s4e.util.AppUserDetailsSupplier;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static pl.cyfronet.s4e.Constants.API_PREFIX_V1;

@RestController
@RequestMapping(path = API_PREFIX_V1, produces = APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "config", description = "The Config API")
public class ConfigController {
    private final GeoServerProperties geoServerProperties;
    private final OsmProperties osmProperties;
    private final KeyValueConfigSupplier keyValueConfigSupplier;
    private final SearchPanelConfigResponseSupplier searchPanelConfigResponseSupplier;

    @Value("${recaptcha.validation.siteKey}")
    private String recaptchaSiteKey;

    @Operation(summary = "Get the front-end app configuration")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK")
    })
    @GetMapping("/config")
    public ConfigResponse config() {
        return ConfigResponse.builder()
                .osmUrl(osmProperties.getUrl())
                .geoserverUrl(geoServerProperties.getOutsideBaseUrl())
                .geoserverWorkspace(geoServerProperties.getWorkspace())
                .recaptchaSiteKey(recaptchaSiteKey)
                .helpdesk(keyValueConfigSupplier.getConfig(Constants.PROPERTY_HELPDESK_CONFIG))
                .analytics(keyValueConfigSupplier.getConfig(Constants.PROPERTY_ANALYTICS_CONFIG))
                .build();
    }

    @Operation(summary = "Get the front-end Sentinel search configuration")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK")
    })
    @GetMapping("/config/search")
    public SearchPanelConfigResponse sentinelSearchConfig() {
        AppUserDetails userDetails = AppUserDetailsSupplier.get();

        return searchPanelConfigResponseSupplier.getConfig(userDetails);
    }
}
