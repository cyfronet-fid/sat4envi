package pl.cyfronet.s4e.controller;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.cyfronet.s4e.bean.Group;
import pl.cyfronet.s4e.controller.request.CreateGroupRequest;
import pl.cyfronet.s4e.controller.request.UpdateGroupRequest;
import pl.cyfronet.s4e.controller.response.GroupResponse;
import pl.cyfronet.s4e.controller.response.MembersResponse;
import pl.cyfronet.s4e.event.OnAddToGroupEvent;
import pl.cyfronet.s4e.event.OnRemoveFromGroupEvent;
import pl.cyfronet.s4e.ex.BadRequestException;
import pl.cyfronet.s4e.ex.GroupCreationException;
import pl.cyfronet.s4e.ex.GroupUpdateException;
import pl.cyfronet.s4e.ex.NotFoundException;
import pl.cyfronet.s4e.service.AppUserService;
import pl.cyfronet.s4e.service.GroupService;
import pl.cyfronet.s4e.service.InstitutionService;
import pl.cyfronet.s4e.service.SlugService;

import javax.validation.Valid;
import java.util.Optional;
import java.util.stream.Collectors;

import static pl.cyfronet.s4e.Constants.API_PREFIX_V1;

@RestController
@RequestMapping(API_PREFIX_V1)
@RequiredArgsConstructor
public class GroupController {
    private final GroupService groupService;
    private final AppUserService appUserService;
    private final InstitutionService institutionService;
    private final ApplicationEventPublisher eventPublisher;
    private final SlugService slugService;

    @ApiOperation("Create a new group")
    @ApiResponses({
            @ApiResponse(code = 200, message = "If group was created"),
            @ApiResponse(code = 400, message = "Group not created"),
            @ApiResponse(code = 404, message = "Institution not found")
    })
    @PostMapping("/institutions/{institution}/groups")
    public ResponseEntity<?> create(@RequestBody @Valid CreateGroupRequest request,
                                    @PathVariable("institution") String institutionSlug)
            throws GroupCreationException, NotFoundException {
        val institution = institutionService.getInstitution(institutionSlug)
                .orElseThrow(() -> new NotFoundException("Institution not found for id '" + institutionSlug));
        Group group = Group.builder()
                .name(request.getName())
                .slug(slugService.slugify(request.getName()))
                .institution(institution)
                .build();
        if (request.getMembersEmails() != null) {
            group.setMembers(request.getMembersEmails().stream()
                    .map(appUserService::findByEmail)
                    .flatMap(Optional::stream)
                    .collect(Collectors.toSet()));
        }
        groupService.save(group);
        return ResponseEntity.ok().build();
    }

    @ApiOperation("Add a new member to the group")
    @ApiResponses({
            @ApiResponse(code = 200, message = "If member was added"),
            @ApiResponse(code = 400, message = "Member not added"),
            @ApiResponse(code = 404, message = "Group or user not found")
    })
    @PostMapping("/institutions/{institution}/groups/{group}/members")
    public ResponseEntity<?> addMember(@RequestBody String email,
                                       @PathVariable("institution") String institutionSlug,
                                       @PathVariable("group") String groupSlug)
            throws NotFoundException, GroupUpdateException, BadRequestException {
        val group = groupService.getGroup(institutionSlug, groupSlug)
                .orElseThrow(() -> new NotFoundException("Group not found for id '" + groupSlug));
        checkInstitution(institutionSlug, group);
        val appUser = appUserService.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found for mail: '" + email));
        group.addMember(appUser);
        groupService.update(group);
        eventPublisher.publishEvent(new OnAddToGroupEvent(appUser, group, LocaleContextHolder.getLocale()));
        return ResponseEntity.ok().build();
    }

    @ApiOperation("Remove a member from the group")
    @ApiResponses({
            @ApiResponse(code = 200, message = "If member was removed"),
            @ApiResponse(code = 400, message = "Member not removed"),
            @ApiResponse(code = 404, message = "Group or user not found")
    })
    @PostMapping("/institutions/{institution}/groups/{group}/members/{email}")
    public ResponseEntity<?> removeMember(@PathVariable("institution") String institutionSlug,
                                          @PathVariable("group") String groupSlug,
                                          @PathVariable String email)
            throws NotFoundException, GroupUpdateException, BadRequestException {
        val group = groupService.getGroup(institutionSlug, groupSlug)
                .orElseThrow(() -> new NotFoundException("Group not found for id '" + groupSlug));
        checkInstitution(institutionSlug, group);
        val appUser = appUserService.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found for mail: '" + email));
        group.removeMember(appUser);
        groupService.update(group);
        eventPublisher.publishEvent(new OnRemoveFromGroupEvent(appUser, group, LocaleContextHolder.getLocale()));
        return ResponseEntity.ok().build();
    }

    @ApiOperation("Get a list of groups")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Successfully retrieved list")
    })
    @GetMapping("/institutions/{institution}/groups")
    public Page<GroupResponse> getAllByInstitutionName(@PathVariable("institution") String institutionSlug,
                                                       Pageable pageable) {
        Page<Group> page = groupService.getAllByInstitution(institutionSlug, pageable);
        return new PageImpl<>(
                page.stream()
                        .map(m -> GroupResponse.of(m))
                        .collect(Collectors.toList()),
                page.getPageable(),
                page.getTotalElements());
    }

    @ApiOperation("Get a group")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Successfully retrieved a group"),
            @ApiResponse(code = 400, message = "Group not retrieved"),
            @ApiResponse(code = 404, message = "Group not found")
    })
    @GetMapping("/institutions/{institution}/groups/{group}")
    public GroupResponse get(@PathVariable("institution") String institutionSlug,
                             @PathVariable("group") String groupSlug)
            throws NotFoundException, BadRequestException {
        val group = groupService.getGroup(institutionSlug, groupSlug)
                .orElseThrow(() -> new NotFoundException("Group not found for id '" + groupSlug));
        checkInstitution(institutionSlug, group);
        return GroupResponse.of(group);
    }

    @ApiOperation("Get members")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Successfully retrieved members"),
            @ApiResponse(code = 400, message = "Members not retrieved"),
            @ApiResponse(code = 404, message = "Members not found")
    })
    @GetMapping("/institutions/{institution}/groups/{group}/members")
    public MembersResponse getMembers(@PathVariable("institution") String institutionSlug,
                                      @PathVariable("group") String groupSlug)
            throws NotFoundException, BadRequestException {
        val group = groupService.getGroup(institutionSlug, groupSlug)
                .orElseThrow(() -> new NotFoundException("Group not found for id '" + groupSlug));
        checkInstitution(institutionSlug, group);
        return MembersResponse.of(groupService.getMembers(institutionSlug, groupSlug));
    }

    @ApiOperation("Update a group")
    @ApiResponses({
            @ApiResponse(code = 200, message = "If group was updated"),
            @ApiResponse(code = 400, message = "Group not updated"),
            @ApiResponse(code = 404, message = "Group was not found")
    })
    @PutMapping("/institutions/{institution}/groups/{group}")
    public ResponseEntity<?> update(@RequestBody UpdateGroupRequest request,
                                    @PathVariable("institution") String institutionSlug,
                                    @PathVariable("group") String groupSlug)
            throws NotFoundException, GroupUpdateException, BadRequestException {
        val group = groupService.getGroup(institutionSlug, groupSlug)
                .orElseThrow(() -> new NotFoundException("Group not found for id '" + groupSlug));
        checkInstitution(institutionSlug, group);
        group.setName(request.getName());
        group.setSlug(slugService.slugify(request.getName()));
        if (request.getMembersEmails() != null) {
            group.setMembers(
                    request.getMembersEmails().stream()
                            .map(appUserService::findByEmail)
                            .flatMap(Optional::stream)
                            .collect(Collectors.toSet()));
        }
        groupService.update(group);
        return ResponseEntity.ok().build();
    }

    @ApiOperation("Delete a group")
    @ApiResponses({
            @ApiResponse(code = 200, message = "If group was deleted"),
            @ApiResponse(code = 400, message = "Group was not deleted"),
            @ApiResponse(code = 404, message = "Group or institution was not found")
    })
    @DeleteMapping("/institutions/{institution}/groups/{group}")
    public ResponseEntity<?> delete(@PathVariable("institution") String institutionSlug,
                                    @PathVariable("group") String groupSlug)
            throws NotFoundException, BadRequestException {
        val group = groupService.getGroup(institutionSlug, groupSlug)
                .orElseThrow(() -> new NotFoundException("Group not found for id '" + groupSlug));
        checkInstitution(institutionSlug, group);
        groupService.delete(group);
        return ResponseEntity.ok().build();
    }

    private void checkInstitution(String institutionSlug, Group group) throws BadRequestException {
        if (!institutionSlug.equals(group.getInstitution().getSlug())) {
            throw new BadRequestException("Institution is not correct");
        }
    }
}
