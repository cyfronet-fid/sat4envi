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
import org.springdoc.core.converters.models.PageableAsQueryParam;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.web.bind.annotation.*;
import pl.cyfronet.s4e.bean.AppRole;
import pl.cyfronet.s4e.controller.request.AddMemberRequest;
import pl.cyfronet.s4e.controller.request.CreateChildInstitutionRequest;
import pl.cyfronet.s4e.controller.request.CreateInstitutionRequest;
import pl.cyfronet.s4e.controller.request.UpdateInstitutionRequest;
import pl.cyfronet.s4e.controller.response.BasicInstitutionResponse;
import pl.cyfronet.s4e.controller.response.InstitutionResponse;
import pl.cyfronet.s4e.controller.response.MemberResponse;
import pl.cyfronet.s4e.event.OnAddToInstitutionEvent;
import pl.cyfronet.s4e.event.OnRemoveFromInstitutionEvent;
import pl.cyfronet.s4e.ex.InstitutionCreationException;
import pl.cyfronet.s4e.ex.NotFoundException;
import pl.cyfronet.s4e.ex.S3ClientException;
import pl.cyfronet.s4e.security.AppUserDetails;
import pl.cyfronet.s4e.service.AppUserService;
import pl.cyfronet.s4e.service.InstitutionService;
import pl.cyfronet.s4e.service.UserRoleService;
import pl.cyfronet.s4e.util.AppUserDetailsSupplier;

import javax.validation.Valid;
import java.util.List;
import java.util.Set;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static pl.cyfronet.s4e.Constants.API_PREFIX_V1;
import static pl.cyfronet.s4e.security.AppUserDetailsUtil.isAdmin;

@RestController
@RequestMapping(path = API_PREFIX_V1, produces = APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "institution", description = "The Institution API")
public class InstitutionController {
    private final InstitutionService institutionService;
    private final AppUserService appUserService;
    private final ApplicationEventPublisher eventPublisher;
    private final UserRoleService userRoleService;

    @Operation(summary = "Create a new institution")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "If institution was created"),
            @ApiResponse(responseCode = "400", description = "Incorrect request", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthenticated", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content)
    })
    @PostMapping(value = "/institutions", consumes = APPLICATION_JSON_VALUE)
    public BasicInstitutionResponse create(@RequestBody @Valid CreateInstitutionRequest request)
            throws InstitutionCreationException, NotFoundException {
        val institutionSlug = institutionService.create(request);
        return institutionService.findBySlug(institutionSlug, BasicInstitutionResponse.class).get();
    }

    @Operation(summary = "Update an institution")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "If institution was updated"),
            @ApiResponse(responseCode = "400", description = "Incorrect request: not updated", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthenticated", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content),
            @ApiResponse(responseCode = "404", description = "Not found", content = @Content)
    })
    @PutMapping(value = "/institutions/{institution}", consumes = APPLICATION_JSON_VALUE)
    public BasicInstitutionResponse update(@RequestBody UpdateInstitutionRequest request,
                       @PathVariable("institution") String institutionSlug)
            throws NotFoundException, S3ClientException {
        val updatedInstitutionSlug = institutionService.update(request, institutionSlug);
        return institutionService.findBySlug(updatedInstitutionSlug, BasicInstitutionResponse.class).get();
    }

    @Operation(summary = "Create a new child institution")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "If institution was created"),
            @ApiResponse(responseCode = "400", description = "Incorrect request", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthenticated", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content),
            @ApiResponse(responseCode = "404", description = "Not found", content = @Content)
    })
    @PostMapping(value = "/institutions/{institution}/child", consumes = APPLICATION_JSON_VALUE)
    public BasicInstitutionResponse createChild(@RequestBody @Valid CreateChildInstitutionRequest request,
                            @PathVariable("institution") String institutionSlug)
            throws InstitutionCreationException, NotFoundException {
        val newInstitutionSlug = institutionService.createChild(request, institutionSlug);
        return institutionService.findBySlug(newInstitutionSlug, BasicInstitutionResponse.class).get();
    }

    @Operation(summary = "Get a list of institutions")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully retrieved list"),
            @ApiResponse(responseCode = "400", description = "Incorrect request", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthenticated", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content),
    })
    @PageableAsQueryParam
    @GetMapping("/institutions")
    public List<BasicInstitutionResponse> getAll() {
        AppUserDetails appUserDetails = AppUserDetailsSupplier.get();
        if (isAdmin(appUserDetails)) {
            return institutionService.getAll(BasicInstitutionResponse.class);
        }
        return institutionService.getUserInstitutionsBy(appUserDetails.getUsername(),
                List.of(AppRole.INST_MEMBER.name()),
                BasicInstitutionResponse.class);
    }

    @Operation(summary = "Get an institution")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully retrieved an institution"),
            @ApiResponse(responseCode = "400", description = "Incorrect request", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthenticated", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content),
            @ApiResponse(responseCode = "404", description = "Not found", content = @Content)
    })
    @GetMapping("/institutions/{institution}")
    public InstitutionResponse get(@PathVariable("institution") String institutionSlug) throws NotFoundException {
        return institutionService.findBySlug(institutionSlug, InstitutionResponse.class)
                .orElseThrow(() -> new NotFoundException("Institution not found for id '" + institutionSlug + "'"));
    }

    @Operation(summary = "Get members")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully retrieved members"),
            @ApiResponse(responseCode = "400", description = "Incorrect request", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthenticated", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content),
            @ApiResponse(responseCode = "404", description = "Not found", content = @Content)
    })
    @GetMapping("/institutions/{institution}/members")
    public Set<MemberResponse> getMembers(@PathVariable("institution") String institutionSlug) {
        return institutionService.getMembers(institutionSlug, MemberResponse.class);
    }

    @Operation(summary = "Add a new member to the institution")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "If member was added"),
            @ApiResponse(responseCode = "400", description = "Incorrect request", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthenticated", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content),
            @ApiResponse(responseCode = "404", description = "Not found", content = @Content)
    })
    @PostMapping(value = "/institutions/{institution}/members", consumes = APPLICATION_JSON_VALUE)
    public void addMemberRole(@RequestBody AddMemberRequest request,
                              @PathVariable("institution") String institutionSlug)
            throws NotFoundException {
        val appUser = appUserService.findByEmail(request.getEmail())
                .orElseThrow(() -> new NotFoundException("User not found for email: '" + request.getEmail() + "'"));
        userRoleService.addRole(institutionSlug, appUser.getId(), AppRole.INST_MEMBER);
        eventPublisher.publishEvent(new OnAddToInstitutionEvent(
                request.getEmail(),
                institutionSlug,
                LocaleContextHolder.getLocale()
        ));
    }

    @Operation(summary = "Remove a member from the institution")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "If member was removed"),
            @ApiResponse(responseCode = "400", description = "Incorrect request", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthenticated", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content),
            @ApiResponse(responseCode = "404", description = "Not found", content = @Content)
    })
    @DeleteMapping("/institutions/{institution}/members/{appUserId}")
    public void removeMemberRole(@PathVariable("institution") String institutionSlug,
                                 @PathVariable Long appUserId)
            throws NotFoundException {
        userRoleService.removeRole(institutionSlug, appUserId, AppRole.INST_MEMBER);

        val appUser = appUserService.findById(appUserId)
                .orElseThrow(() -> new NotFoundException("User not found for id: '" + appUserId + "'"));
        eventPublisher.publishEvent(new OnRemoveFromInstitutionEvent(
                appUser.getEmail(),
                institutionSlug,
                LocaleContextHolder.getLocale()
        ));
    }

    @Operation(summary = "Add admin role to the institution")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Role has been added"),
            @ApiResponse(responseCode = "401", description = "Unauthenticated", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content)
    })
    @PostMapping("/institutions/{institution}/admins/{appUserId}")
    public void addAdminRole(
            @PathVariable("institution") String institutionSlug,
            @PathVariable Long appUserId
    )
            throws NotFoundException {
        userRoleService.addRole(institutionSlug, appUserId, AppRole.INST_ADMIN);

        // TODO: Send confirmation email to user
    }

    @Operation(summary = "Remove admin role from the institution")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Role has been removed"),
            @ApiResponse(responseCode = "401", description = "Unauthenticated", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content)
    })
    @DeleteMapping("/institutions/{institution}/admins/{appUserId}")
    public void removeAdminRole(
            @PathVariable("institution") String institutionSlug,
            @PathVariable Long appUserId
    )
            throws NotFoundException {
        userRoleService.removeRole(institutionSlug, appUserId, AppRole.INST_ADMIN);

        // TODO: Send confirmation email to user
    }

    @Operation(summary = "Delete an institution")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "If institution was deleted"),
            @ApiResponse(responseCode = "400", description = "Incorrect request", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthenticated", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content),
            @ApiResponse(responseCode = "404", description = "Not found", content = @Content)
    })
    @DeleteMapping("/institutions/{institution}")
    public void delete(@PathVariable("institution") String institutionSlug) {
        institutionService.delete(institutionSlug);
    }
}
