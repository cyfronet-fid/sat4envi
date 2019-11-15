package pl.cyfronet.s4e.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springdoc.core.converters.PageableAsQueryParam;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pl.cyfronet.s4e.bean.Group;
import pl.cyfronet.s4e.controller.request.CreateGroupRequest;
import pl.cyfronet.s4e.controller.request.UpdateGroupRequest;
import pl.cyfronet.s4e.controller.response.GroupResponse;
import pl.cyfronet.s4e.controller.response.MembersResponse;
import pl.cyfronet.s4e.ex.GroupCreationException;
import pl.cyfronet.s4e.ex.NotFoundException;
import pl.cyfronet.s4e.service.GroupService;

import javax.validation.Valid;
import java.util.stream.Collectors;

import static pl.cyfronet.s4e.Constants.API_PREFIX_V1;

@RestController
@RequestMapping(API_PREFIX_V1)
@RequiredArgsConstructor
@Tag(name = "group", description = "The Group API")
@PreAuthorize("isAuthenticated()")
public class GroupController {
    private final GroupService groupService;

    @Operation(summary = "Create a new group")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "If group was created"),
            @ApiResponse(responseCode = "400", description = "Group not created"),
            @ApiResponse(responseCode = "403", description = "Forbidden: Don't have permission to create a group"),
            @ApiResponse(responseCode = "404", description = "Institution not found")
    })
    @PostMapping("/institutions/{institution}/groups")
    @PreAuthorize("isInstitutionManager(#institutionSlug)")
    public ResponseEntity<?> create(@RequestBody @Valid CreateGroupRequest request,
                                    @PathVariable("institution") String institutionSlug)
            throws GroupCreationException, NotFoundException {
        groupService.createFromRequest(request, institutionSlug);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Add a new member to the group")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "If member was added"),
            @ApiResponse(responseCode = "400", description = "Member not added"),
            @ApiResponse(responseCode = "403", description = "Forbidden: Don't have permission to add member"),
            @ApiResponse(responseCode = "404", description = "Group or user not found")
    })
    @PostMapping("/institutions/{institution}/groups/{group}/members")
    @PreAuthorize("isGroupManager(#institutionSlug, #groupSlug) || isInstitutionManager(#institutionSlug)")
    public ResponseEntity<?> addMember(@RequestBody String email,
                                       @PathVariable("institution") String institutionSlug,
                                       @PathVariable("group") String groupSlug)
            throws NotFoundException {
        groupService.addMember(institutionSlug, groupSlug, email);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Remove a member from the group")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "If member was removed"),
            @ApiResponse(responseCode = "400", description = "Member not removed"),
            @ApiResponse(responseCode = "403", description = "Forbidden: Don't have permission to remove member"),
            @ApiResponse(responseCode = "404", description = "Group or user not found")
    })
    @PostMapping("/institutions/{institution}/groups/{group}/members/{email}")
    @PreAuthorize("isGroupManager(#institutionSlug, #groupSlug) || isInstitutionManager(#institutionSlug)")
    public ResponseEntity<?> removeMember(@PathVariable("institution") String institutionSlug,
                                          @PathVariable("group") String groupSlug,
                                          @PathVariable String email)
            throws NotFoundException {
        groupService.removeMember(institutionSlug, groupSlug, email);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Get a list of groups")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully retrieved list"),
            @ApiResponse(responseCode = "403", description = "Forbidden: Don't have permission to get a list")
    })
    @PageableAsQueryParam
    @GetMapping("/institutions/{institution}/groups")
    @PreAuthorize("isGroupManager(#institutionSlug, #groupSlug) || isInstitutionManager(#institutionSlug)")
    public Page<GroupResponse> getAllByInstitutionName(@PathVariable("institution") String institutionSlug,
                                                       @Parameter(hidden = true) Pageable pageable) {
        Page<Group> page = groupService.getAllByInstitution(institutionSlug, pageable);
        return new PageImpl<>(
                page.stream()
                        .map(GroupResponse::of)
                        .collect(Collectors.toList()),
                page.getPageable(),
                page.getTotalElements());
    }

    @Operation(summary = "Get a group")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully retrieved a group"),
            @ApiResponse(responseCode = "400", description = "Group not retrieved"),
            @ApiResponse(responseCode = "403", description = "Forbidden: Don't have permission to get a group"),
            @ApiResponse(responseCode = "404", description = "Group not found")
    })
    @GetMapping("/institutions/{institution}/groups/{group}")
    @PreAuthorize("isGroupManager(#institutionSlug, #groupSlug) || isInstitutionManager(#institutionSlug)")
    public GroupResponse get(@PathVariable("institution") String institutionSlug,
                             @PathVariable("group") String groupSlug)
            throws NotFoundException {
        val group = groupService.getGroup(institutionSlug, groupSlug)
                .orElseThrow(() -> new NotFoundException("Group not found for id '" + groupSlug));
        return GroupResponse.of(group);
    }

    @Operation(summary = "Get members")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully retrieved members"),
            @ApiResponse(responseCode = "400", description = "Members not retrieved"),
            @ApiResponse(responseCode = "403", description = "Forbidden: Don't have permission to get members")
    })
    @GetMapping("/institutions/{institution}/groups/{group}/members")
    @PreAuthorize("isGroupManager(#institutionSlug, #groupSlug) || isInstitutionManager(#institutionSlug)")
    public MembersResponse getMembers(@PathVariable("institution") String institutionSlug,
                                      @PathVariable("group") String groupSlug) {
        return MembersResponse.of(groupService.getMembers(institutionSlug, groupSlug));
    }

    @Operation(summary = "Update a group")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "If group was updated"),
            @ApiResponse(responseCode = "400", description = "Group not updated"),
            @ApiResponse(responseCode = "403", description = "Forbidden: Don't have permission to update a group"),
            @ApiResponse(responseCode = "404", description = "Group was not found")
    })
    @PutMapping("/institutions/{institution}/groups/{group}")
    @PreAuthorize("isGroupManager(#institutionSlug, #groupSlug) || isInstitutionManager(#institutionSlug)")
    public ResponseEntity<?> update(@RequestBody @Valid UpdateGroupRequest request,
                                    @PathVariable("institution") String institutionSlug,
                                    @PathVariable("group") String groupSlug)
            throws NotFoundException {
        groupService.updateFromRequest(request, institutionSlug, groupSlug);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Delete a group")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "If group was deleted"),
            @ApiResponse(responseCode = "400", description = "Group was not deleted"),
            @ApiResponse(responseCode = "403", description = "Forbidden: Don't have permission to delete a group"),
            @ApiResponse(responseCode = "404", description = "Group or institution was not found")
    })
    @DeleteMapping("/institutions/{institution}/groups/{group}")
    @PreAuthorize("isInstitutionManager(#institutionSlug)")
    public ResponseEntity<?> delete(@PathVariable("institution") String institutionSlug,
                                    @PathVariable("group") String groupSlug)
            throws NotFoundException {
        groupService.deleteBySlugs(institutionSlug, groupSlug);
        return ResponseEntity.ok().build();
    }
}
