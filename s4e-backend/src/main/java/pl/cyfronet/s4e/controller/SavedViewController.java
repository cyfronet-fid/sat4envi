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
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.converters.models.PageableAsQueryParam;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.SortDefault;
import org.springframework.web.bind.annotation.*;
import pl.cyfronet.s4e.controller.request.CreateSavedViewRequest;
import pl.cyfronet.s4e.controller.response.SavedViewResponse;
import pl.cyfronet.s4e.ex.NotFoundException;
import pl.cyfronet.s4e.security.AppUserDetails;
import pl.cyfronet.s4e.service.SavedViewService;
import pl.cyfronet.s4e.util.AppUserDetailsSupplier;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.UUID;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static pl.cyfronet.s4e.Constants.API_PREFIX_V1;

@RestController
@RequestMapping(path = API_PREFIX_V1, produces = APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "savedView", description = "The SavedView API")
public class SavedViewController {
    private final SavedViewService savedViewService;

    @Operation(
            summary = "Create a new SavedView",
            description =
                    "Create a new SavedView, which will have the owner set to the authenticated AppUser. " +
                            "The createdAt is set to the current datetime."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Operation successful"),
            @ApiResponse(responseCode = "400", description = "Incorrect request", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthenticated", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content)
    })
    @PostMapping(value = "/saved-views", consumes = APPLICATION_JSON_VALUE)
    public SavedViewResponse create(@RequestBody @Valid CreateSavedViewRequest request) throws NotFoundException {
        AppUserDetails userDetails = AppUserDetailsSupplier.get();
        UUID id = savedViewService.create(SavedViewService.Create.builder()
                .caption(request.getCaption())
                .thumbnail(Base64.getDecoder().decode(request.getThumbnail()))
                .configuration(request.getConfiguration())
                .ownerEmail(userDetails.getEmail())
                .createdAt(LocalDateTime.now())
                .build());
        return savedViewService.findById(id, SavedViewResponse.class)
                // This shouldn't happen, so throw a runtime exception.
                .orElseThrow(() -> new IllegalStateException("SavedView not found for id '" + id +"'"));
    }

    @Operation(summary = "List SavedViews of authenticated user")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Operation successful"),
            @ApiResponse(responseCode = "401", description = "Unauthenticated", content = @Content)
    })
    @PageableAsQueryParam
    @GetMapping("/saved-views")
    public Page<SavedViewResponse> list(
            @Parameter(hidden = true) @SortDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        AppUserDetails userDetails = AppUserDetailsSupplier.get();
        return savedViewService.listByAppUser(userDetails.getEmail(), pageable);
    }

    @Operation(summary = "Delete a SavedView")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Operation successful"),
            @ApiResponse(responseCode = "400", description = "Incorrect request", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthenticated", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content)
    })
    @DeleteMapping("/saved-views/{uuid}")
    public void delete(@PathVariable @Parameter(schema = @Schema(format = "uuid")) UUID uuid) {
        savedViewService.delete(uuid);
    }
}
