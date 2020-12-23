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

package pl.cyfronet.s4e.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.cyfronet.s4e.Constants;
import pl.cyfronet.s4e.controller.request.ExpertHelpRequest;
import pl.cyfronet.s4e.data.repository.PropertyRepository;
import pl.cyfronet.s4e.event.OnSendHelpRequestEvent;
import pl.cyfronet.s4e.util.AppUserDetailsSupplier;

import javax.validation.Valid;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static pl.cyfronet.s4e.Constants.API_PREFIX_V1;

@RestController
@RequestMapping(path = API_PREFIX_V1, produces = APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "expertHelp", description = "Send issue to helpdesk")
public class ExpertHelpController {
    private final ApplicationEventPublisher eventPublisher;
    private final PropertyRepository propertyRepository;

    private interface PropertyProjection {
        String getName();
        String getValue();
    }

    @Operation(summary = "Send expert help request email from user")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Help request was send"),
            @ApiResponse(responseCode = "400", description = "Incorrect request", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthenticated", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content)
    })
    @PostMapping(value = "/expert-help", consumes = APPLICATION_JSON_VALUE)
    public void sendHelpRequest(@RequestBody @Valid ExpertHelpRequest request) {
        propertyRepository.findByName(Constants.PROPERTY_EXPERT_HELP_EMAIL, PropertyProjection.class)
                .map(PropertyProjection::getValue)
                .map(expertEmail -> {
                    val requestingUserEmail = AppUserDetailsSupplier.get().getEmail();
                    return new OnSendHelpRequestEvent(
                            requestingUserEmail,
                            expertEmail,
                            request.getHelpType(),
                            request.getIssueDescription(),
                            LocaleContextHolder.getLocale()
                    );
                })
                .ifPresentOrElse(eventPublisher::publishEvent, () -> {
                    throw new IllegalStateException(
                            "Cannot send request, expert email not configured " +
                            "(property " + Constants.PROPERTY_EXPERT_HELP_EMAIL + ")"
                    );
                });
    }
}
