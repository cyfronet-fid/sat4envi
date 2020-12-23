/*
 * Copyright 2020 ACC Cyfronet AGH
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

package pl.cyfronet.gsg;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotEmpty;
import java.time.Duration;
import java.util.HashSet;
import java.util.Set;

@ConfigurationProperties(prefix = "gateway")
@Validated
@Getter
@Setter
public class GatewayProperties {
    @NotEmpty
    private String geoserverUri;

    private Set<String> allowedParams = new HashSet<>();

    private Set<String> openLayers = new HashSet<>();

    private Set<String> eumetsatLayers = new HashSet<>();

    private QueryParams queryParams = new QueryParams();

    private Duration freshDuration = Duration.ofHours(3);

    @Validated
    @Getter
    @Setter
    public static class QueryParams {
        private String layers = "LAYERS";
        private String time = "TIME";
    }
}
