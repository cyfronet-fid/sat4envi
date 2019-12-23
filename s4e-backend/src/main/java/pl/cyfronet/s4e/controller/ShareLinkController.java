package pl.cyfronet.s4e.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.cyfronet.s4e.bean.AppUser;
import pl.cyfronet.s4e.controller.request.ShareLinkRequest;
import pl.cyfronet.s4e.event.OnShareLinkEvent;
import pl.cyfronet.s4e.ex.NotFoundException;
import pl.cyfronet.s4e.security.AppUserDetails;
import pl.cyfronet.s4e.service.AppUserService;

import javax.validation.Valid;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static pl.cyfronet.s4e.Constants.API_PREFIX_V1;

@RestController
@RequestMapping(path = API_PREFIX_V1, produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "shareLink", description = "The Share link API")
@PreAuthorize("isAuthenticated()")
public class ShareLinkController {
    private final ApplicationEventPublisher eventPublisher;
    private final AppUserService appUserService;

    @Operation(
            summary = "Share link",
            description =
                    "Send email with shared linked for user of ZK to access"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Operation successful", content = @Content),
            @ApiResponse(responseCode = "403", description = "Not authenticated", content = @Content),
            @ApiResponse(responseCode = "404", description = "Not found", content = @Content)
    })
    @PostMapping("/share-link")
    @PreAuthorize("isZKMember()")
    public ResponseEntity<?> shareLink(@RequestBody @Valid ShareLinkRequest request) throws NotFoundException {
        AppUserDetails appUserDetails = (AppUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        AppUser appUser = appUserService.findByEmailWithRolesAndGroupsAndInstitution(appUserDetails.getUsername())
                .orElseThrow(() -> new NotFoundException("User not found for email: '" + appUserDetails.getUsername() + "'"));
        eventPublisher.publishEvent(new OnShareLinkEvent(appUser, request.getLink(), request.getEmails(), LocaleContextHolder.getLocale()));
        return ResponseEntity.ok().build();
    }
}
