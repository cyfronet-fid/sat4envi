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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import pl.cyfronet.s4e.bean.Invitation;
import pl.cyfronet.s4e.controller.request.InvitationRequest;
import pl.cyfronet.s4e.controller.request.InvitationResendInvitation;
import pl.cyfronet.s4e.controller.response.BasicInstitutionResponse;
import pl.cyfronet.s4e.controller.response.BasicInvitationResponse;
import pl.cyfronet.s4e.event.OnConfirmInvitationEvent;
import pl.cyfronet.s4e.event.OnDeleteInvitationEvent;
import pl.cyfronet.s4e.event.OnRejectInvitationEvent;
import pl.cyfronet.s4e.event.OnSendInvitationEvent;
import pl.cyfronet.s4e.ex.NotFoundException;
import pl.cyfronet.s4e.service.InvitationService;

import javax.validation.Valid;
import java.util.Set;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static pl.cyfronet.s4e.Constants.API_PREFIX_V1;

@RestController
@RequestMapping(path = API_PREFIX_V1, produces = APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "invitation", description = "Invite by email API")
public class InvitationController {
    private final InvitationService invitationService;
    private final ApplicationEventPublisher eventPublisher;

    @Operation(summary = "Fetch all invitations for specific institution")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully retrieved list"),
            @ApiResponse(responseCode = "400", description = "Incorrect request", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthenticated", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content)
    })
    @GetMapping(value = "/institutions/{institution}/invitations")
    public Set<BasicInvitationResponse> get(@PathVariable("institution") String institutionSlug) {
        return invitationService.findAllBy(institutionSlug, BasicInvitationResponse.class);
    }

    @Operation(summary = "Send invitation email to user")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Invitation was send"),
            @ApiResponse(responseCode = "400", description = "Incorrect request", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthenticated", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content),
            @ApiResponse(responseCode = "404", description = "Not found", content = @Content)
    })
    @PostMapping(value = "/institutions/{institution}/invitations", consumes = APPLICATION_JSON_VALUE)
    public BasicInvitationResponse send(
            @RequestBody @Valid InvitationRequest request,
            @PathVariable("institution") String institutionSlug
    ) throws Exception {
        val token = invitationService.createInvitationFrom(
                request.getEmail(),
                institutionSlug,
                request.isForAdmin()
        );
        val invitationEvent = new OnSendInvitationEvent(token, LocaleContextHolder.getLocale());
        eventPublisher.publishEvent(invitationEvent);

        return invitationService.findByToken(token, BasicInvitationResponse.class).get();
    }

    @Transactional
    @Operation(summary = "Resend invitation and delete last one")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Invitation was resend"),
            @ApiResponse(
                    responseCode = "400",
                    description = "Institution in url and invitation institution are different",
                    content = @Content
            ),
            @ApiResponse(responseCode = "401", description = "Unauthenticated", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content),
            @ApiResponse(responseCode = "404", description = "Not found", content = @Content)
    })
    @PutMapping(value = "/institutions/{institution}/invitations", consumes = APPLICATION_JSON_VALUE)
    public BasicInvitationResponse resend(
            @RequestBody @Valid InvitationResendInvitation request,
            @PathVariable("institution") String institutionSlug
    ) throws Exception {
        val invitation = invitationService
                .findByEmailAndInstitutionSlug(request.getOldEmail(), institutionSlug, Invitation.class)
                .orElseThrow(() -> new NotFoundException("Invitation couldn't be found"));
        invitationService.deleteBy(invitation.getToken());

        val newToken = invitationService.createInvitationFrom(
                request.getNewEmail(),
                institutionSlug,
                request.isForAdmin()
        );
        val invitationEvent = new OnSendInvitationEvent(newToken, LocaleContextHolder.getLocale());
        eventPublisher.publishEvent(invitationEvent);

        return invitationService.findByToken(newToken, BasicInvitationResponse.class).get();
    }

    @Transactional
    @Operation(summary = "Delete invitation")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Invitation have been removed"),
            @ApiResponse(
                    responseCode = "400",
                    description = "Institution in url and invitation institution are different",
                    content = @Content
            ),
            @ApiResponse(responseCode = "401", description = "Unauthenticated", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content),
            @ApiResponse(responseCode = "404", description = "Not found", content = @Content)
    })
    @DeleteMapping(value = "/institutions/{institution}/invitations/{id}")
    public void delete(
            @PathVariable("institution") String institutionSlug,
            @PathVariable Long id
    ) throws Exception {
        val invitation = invitationService
                .findByIdAndInstitutionSlug(id, institutionSlug, Invitation.class)
                .orElseThrow(() -> new NotFoundException("Invitation couldn't be found"));

        val deleteInvitationEvent = new OnDeleteInvitationEvent(invitation.getToken(), LocaleContextHolder.getLocale());
        eventPublisher.publishEvent(deleteInvitationEvent);
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
                .orElseThrow(() -> new NotFoundException("Institution couldn't be found"));
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
