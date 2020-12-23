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
public class UpdateInstitutionRequest {
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
}
