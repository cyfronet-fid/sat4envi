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
import pl.cyfronet.s4e.controller.request.ShareLinkRequest;
import pl.cyfronet.s4e.event.OnShareLinkEvent;
import pl.cyfronet.s4e.util.AppUserDetailsSupplier;

import javax.validation.Valid;
import java.util.Base64;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static pl.cyfronet.s4e.Constants.API_PREFIX_V1;

@RestController
@RequestMapping(path = API_PREFIX_V1, produces = APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "shareLink", description = "The Share link API")
public class ShareLinkController {
    private final ApplicationEventPublisher eventPublisher;

    @Operation(
            summary = "Share link",
            description =
                    "Send email with shared link for user of ZK to access"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Operation successful", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthenticated", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content)
    })
    @PostMapping(value = "/share-link", consumes = APPLICATION_JSON_VALUE)
    public void shareLink(@RequestBody @Valid ShareLinkRequest request) {
        String requesterEmail = AppUserDetailsSupplier.get().getEmail();
        val shareRequest = OnShareLinkEvent.Request.builder()
                .caption(request.getCaption())
                .description(request.getDescription())
                .thumbnail(Base64.getDecoder().decode(request.getThumbnail()))
                .path(request.getPath())
                .emails(request.getEmails())
                .build();
        eventPublisher.publishEvent(new OnShareLinkEvent(requesterEmail, shareRequest, LocaleContextHolder.getLocale()));
    }
}
