package pl.cyfronet.s4e.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.converters.PageableAsQueryParam;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pl.cyfronet.s4e.controller.request.CreateGroupRequest;
import pl.cyfronet.s4e.controller.request.UpdateGroupRequest;
import pl.cyfronet.s4e.controller.response.AppUserResponse;
import pl.cyfronet.s4e.controller.response.GroupResponse;
import pl.cyfronet.s4e.event.OnAddToGroupEvent;
import pl.cyfronet.s4e.event.OnRemoveFromGroupEvent;
import pl.cyfronet.s4e.ex.GroupCreationException;
import pl.cyfronet.s4e.ex.NotFoundException;
import pl.cyfronet.s4e.service.GroupService;

import javax.validation.Valid;
import java.util.Set;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static pl.cyfronet.s4e.Constants.API_PREFIX_V1;

@RestController
@RequestMapping(path = API_PREFIX_V1, produces = APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "group", description = "The Group API")
public class GroupController {
    private final GroupService groupService;
    private final ApplicationEventPublisher eventPublisher;

    @Operation(summary = "Create a new group")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "If group was created"),
            @ApiResponse(responseCode = "400", description = "Group not created"),
            @ApiResponse(responseCode = "403", description = "Forbidden: Don't have permission to create a group"),
            @ApiResponse(responseCode = "404", description = "Institution not found")
    })
    @PostMapping(value = "/institutions/{institution}/groups", consumes = APPLICATION_JSON_VALUE)
    @PreAuthorize("isAuthenticated() && isInstitutionManager(#institutionSlug)")
    public void create(@RequestBody @Valid CreateGroupRequest request,
                                    @PathVariable("institution") String institutionSlug)
            throws GroupCreationException, NotFoundException {
        groupService.createFromRequest(request, institutionSlug);
    }

    @Operation(summary = "Add a new member to the group")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "If member was added"),
            @ApiResponse(responseCode = "400", description = "Member not added"),
            @ApiResponse(responseCode = "403", description = "Forbidden: Don't have permission to add member"),
            @ApiResponse(responseCode = "404", description = "Group or user not found")
    })
    @PostMapping(value = "/institutions/{institution}/groups/{group}/members", consumes = APPLICATION_JSON_VALUE)
    @PreAuthorize("isAuthenticated() && isGroupManager(#institutionSlug, #groupSlug) || isInstitutionManager(#institutionSlug)")
    public void addMember(@RequestBody String email,
                                       @PathVariable("institution") String institutionSlug,
                                       @PathVariable("group") String groupSlug)
            throws NotFoundException {
        groupService.addMember(institutionSlug, groupSlug, email);
        eventPublisher.publishEvent(new OnAddToGroupEvent(email, groupSlug, institutionSlug, LocaleContextHolder.getLocale()));
    }

    @Operation(summary = "Remove a member from the group")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "If member was removed"),
            @ApiResponse(responseCode = "400", description = "Member not removed"),
            @ApiResponse(responseCode = "403", description = "Forbidden: Don't have permission to remove member"),
            @ApiResponse(responseCode = "404", description = "Group or user not found")
    })
    @PostMapping("/institutions/{institution}/groups/{group}/members/{email}")
    @PreAuthorize("isAuthenticated() && isGroupManager(#institutionSlug, #groupSlug) || isInstitutionManager(#institutionSlug)")
    public void removeMember(@PathVariable("institution") String institutionSlug,
                                          @PathVariable("group") String groupSlug,
                                          @PathVariable String email)
            throws NotFoundException {
        groupService.removeMember(institutionSlug, groupSlug, email);
        eventPublisher.publishEvent(new OnRemoveFromGroupEvent(email, groupSlug, institutionSlug, LocaleContextHolder.getLocale()));
    }

    @Operation(summary = "Get a list of groups")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully retrieved list"),
            @ApiResponse(responseCode = "403", description = "Forbidden: Don't have permission to get a list")
    })
    @PageableAsQueryParam
    @GetMapping("/institutions/{institution}/groups")
    @PreAuthorize("isAuthenticated() && (isGroupManager(#institutionSlug, #groupSlug) || isInstitutionManager(#institutionSlug))")
    public Page<GroupResponse> getAllByInstitutionName(@PathVariable("institution") String institutionSlug,
                                                       @Parameter(hidden = true) Pageable pageable) {
        return groupService.getAllByInstitution(institutionSlug, pageable, GroupResponse.class);
    }

    @Operation(summary = "Get a group")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully retrieved a group"),
            @ApiResponse(responseCode = "400", description = "Group not retrieved"),
            @ApiResponse(responseCode = "403", description = "Forbidden: Don't have permission to get a group"),
            @ApiResponse(responseCode = "404", description = "Group not found")
    })
    @GetMapping("/institutions/{institution}/groups/{group}")
    @PreAuthorize("isAuthenticated() && (isGroupManager(#institutionSlug, #groupSlug) || isInstitutionManager(#institutionSlug))")
    public GroupResponse get(@PathVariable("institution") String institutionSlug,
                             @PathVariable("group") String groupSlug)
            throws NotFoundException {
        return groupService.getGroup(institutionSlug, groupSlug, GroupResponse.class)
                .orElseThrow(() -> new NotFoundException("Group not found for id '" + groupSlug));
    }

    @Operation(summary = "Get members")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully retrieved members"),
            @ApiResponse(responseCode = "400", description = "Members not retrieved"),
            @ApiResponse(responseCode = "403", description = "Forbidden: Don't have permission to get members")
    })
    @GetMapping("/institutions/{institution}/groups/{group}/members")
    @PreAuthorize("isAuthenticated() && (isGroupManager(#institutionSlug, #groupSlug) || isInstitutionManager(#institutionSlug))")
    public Set<AppUserResponse> getMembers(@PathVariable("institution") String institutionSlug,
                                           @PathVariable("group") String groupSlug) {
        return groupService.getMembers(institutionSlug, groupSlug, AppUserResponse.class);
    }

    @Operation(summary = "Update a group")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "If group was updated"),
            @ApiResponse(responseCode = "400", description = "Group not updated"),
            @ApiResponse(responseCode = "403", description = "Forbidden: Don't have permission to update a group"),
            @ApiResponse(responseCode = "404", description = "Group was not found")
    })
    @PutMapping(value = "/institutions/{institution}/groups/{group}", consumes = APPLICATION_JSON_VALUE)
    @PreAuthorize("isAuthenticated() && (isGroupManager(#institutionSlug, #groupSlug) || isInstitutionManager(#institutionSlug))")
    public void update(@RequestBody @Valid UpdateGroupRequest request,
                                    @PathVariable("institution") String institutionSlug,
                                    @PathVariable("group") String groupSlug)
            throws NotFoundException {
        groupService.updateFromRequest(request, institutionSlug, groupSlug);
    }

    @Operation(summary = "Delete a group")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "If group was deleted"),
            @ApiResponse(responseCode = "400", description = "Group was not deleted"),
            @ApiResponse(responseCode = "403", description = "Forbidden: Don't have permission to delete a group"),
            @ApiResponse(responseCode = "404", description = "Group or institution was not found")
    })
    @DeleteMapping("/institutions/{institution}/groups/{group}")
    @PreAuthorize("isAuthenticated() && isInstitutionManager(#institutionSlug)")
    public void delete(@PathVariable("institution") String institutionSlug,
                                    @PathVariable("group") String groupSlug)
            throws NotFoundException {
        groupService.deleteBySlugs(institutionSlug, groupSlug);
    }
}
