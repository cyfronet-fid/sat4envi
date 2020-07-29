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
import org.springframework.web.bind.annotation.*;
import pl.cyfronet.s4e.ex.NotFoundException;
import pl.cyfronet.s4e.controller.request.CreateInvitationRequest;
import pl.cyfronet.s4e.controller.response.BasicInstitutionResponse;
import pl.cyfronet.s4e.event.OnConfirmInvitationEvent;
import pl.cyfronet.s4e.event.OnRejectInvitationEvent;
import pl.cyfronet.s4e.event.OnSendInvitationEvent;
import pl.cyfronet.s4e.service.InvitationService;

import javax.validation.Valid;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static pl.cyfronet.s4e.Constants.API_PREFIX_V1;

@RestController
@RequestMapping(path = API_PREFIX_V1, produces = APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "invitation", description = "Invite by email API")
public class InvitationController {
    private final InvitationService invitationService;
    private final ApplicationEventPublisher eventPublisher;

    @Operation(summary = "Send invitation email to user")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Invitation was send"),
            @ApiResponse(responseCode = "400", description = "Incorrect request", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthenticated", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content),
            @ApiResponse(responseCode = "404", description = "Not found", content = @Content)
    })
    @PostMapping(value = "/institutions/{institution}/invitations", consumes = APPLICATION_JSON_VALUE)
    public void send(
            @RequestBody @Valid CreateInvitationRequest request,
            @PathVariable("institution") String institutionSlug
    ) throws Exception {
        val token = invitationService.createInvitationFrom(request.getEmail(), institutionSlug);
        val invitationEvent = new OnSendInvitationEvent(token, LocaleContextHolder.getLocale());
        eventPublisher.publishEvent(invitationEvent);
    }

    @Operation(summary = "Confirm invitation")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Invitation have been confirmed"),
            @ApiResponse(responseCode = "400", description = "Incorrect request", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthenticated", content = @Content),
            @ApiResponse(responseCode = "404", description = "Not found", content = @Content)
    })
    @PostMapping(value = "/invitations/{token}/confirm")
    public BasicInstitutionResponse confirm(@PathVariable String token) throws NotFoundException {
        invitationService.confirmInvitation(token);
        val institution = invitationService.findInstitutionBy(token, BasicInstitutionResponse.class)
                .orElseThrow(() -> new NotFoundException("Institution couldn't be found"));;
        val confirmInvitationEvent = new OnConfirmInvitationEvent(token, LocaleContextHolder.getLocale());
        eventPublisher.publishEvent(confirmInvitationEvent);

        return institution;
    }

    @Operation(summary = "Reject invitation")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Invitation have been rejected"),
            @ApiResponse(responseCode = "400", description = "Incorrect request", content = @Content),
            @ApiResponse(responseCode = "404", description = "Not found", content = @Content)
    })
    @PutMapping(value = "/invitations/{token}/reject")
    public void reject(@PathVariable String token) throws NotFoundException {
        invitationService.rejectInvitation(token);
        val rejectInvitationEvent = new OnRejectInvitationEvent(token, LocaleContextHolder.getLocale());
        eventPublisher.publishEvent(rejectInvitationEvent);
    }
}
