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

package pl.cyfronet.s4e.controller.response;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.beans.factory.annotation.Value;

import java.time.ZonedDateTime;
import java.util.List;

public interface ReportTemplateResponse {
    @Value("#{target.id}")
    @Schema(format = "uuid")
    String getUuid();

    String getCaption();

    String getNotes();

    List<Long> getOverlayIds();

    Long getProductId();

    @Value("#{@timeHelper.getZonedDateTimeWithBaseZone(target.createdAt)}")
    ZonedDateTime getCreatedAt();
}
