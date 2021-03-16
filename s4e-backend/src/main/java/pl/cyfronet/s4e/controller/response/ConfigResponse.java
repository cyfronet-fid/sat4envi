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

package pl.cyfronet.s4e.controller.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Value;

import java.util.Map;

@Value
@Builder
public class ConfigResponse {
    @Schema(example = "/osm/{z}/{x}/{y}.png")
    String osmUrl;
    @Schema(example = "/wms")
    String geoserverUrl;
    @Schema(example = "development")
    String geoserverWorkspace;
    @Schema(example = "6LeIxAcTAAAAAJcZVRqyHh71UMIEGNQ_MXjiZKhI")
    String recaptchaSiteKey;
    Map<String, String> helpdesk;
    Map<String, String> analytics;
}
