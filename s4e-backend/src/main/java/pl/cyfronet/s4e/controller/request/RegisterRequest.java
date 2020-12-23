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
import pl.cyfronet.s4e.bean.AppUser;
import pl.cyfronet.s4e.controller.validation.CountryCode;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
@Builder
public class RegisterRequest {
    @NotEmpty
    @Email
    private String email;

    @NotEmpty
    private String name;

    @NotEmpty
    private String surname;

    @NotEmpty
    @Size(min=8)
    private String password;

    @NotNull
    @Schema(description = "User's scientific domain")
    private AppUser.ScientificDomain domain;

    @NotNull
    @Schema(description = "User's usage scenario")
    private AppUser.Usage usage;

    @NotNull
    @CountryCode
    @Schema(description = "User's ISO 3166 alpha-2 country code", example = "PL")
    private String country;
}
