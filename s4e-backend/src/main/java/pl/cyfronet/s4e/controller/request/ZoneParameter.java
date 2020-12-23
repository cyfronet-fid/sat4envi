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
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.ZoneId;

/**
 * Make sure to use <code>@RequestParam(defaultValue = "UTC")</code> when using it as a request param to match schema.
 */
@RequiredArgsConstructor
@Getter
@Schema(type = "string", format = "timezone", example = "Europe/Warsaw", defaultValue = "UTC")
public class ZoneParameter {
    private final ZoneId zoneId;
}
