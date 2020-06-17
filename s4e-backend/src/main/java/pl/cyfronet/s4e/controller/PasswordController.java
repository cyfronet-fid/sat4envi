package pl.cyfronet.s4e.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.web.bind.annotation.*;
import pl.cyfronet.s4e.controller.request.PasswordChangeRequest;
import pl.cyfronet.s4e.controller.request.PasswordResetRequest;
import pl.cyfronet.s4e.event.OnPasswordResetTokenEmailEvent;
import pl.cyfronet.s4e.ex.BadRequestException;
import pl.cyfronet.s4e.ex.NotFoundException;
import pl.cyfronet.s4e.ex.PasswordResetTokenExpiredException;
import pl.cyfronet.s4e.security.AppUserDetails;
import pl.cyfronet.s4e.service.AppUserService;
import pl.cyfronet.s4e.service.PasswordService;
import pl.cyfronet.s4e.util.AppUserDetailsSupplier;

import javax.validation.Valid;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static pl.cyfronet.s4e.Constants.API_PREFIX_V1;

@RestController
@RequestMapping(path = API_PREFIX_V1, produces = APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "password", description = "The Password API")
public class PasswordController {
    private final PasswordService passwordService;
    private final AppUserService appUserService;
    private final ApplicationEventPublisher eventPublisher;

    @Operation(summary = "Create and send a password reset token based on email")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Email with a password reset token was sent provided user with it existed"),
            @ApiResponse(responseCode = "400", description = "Incorrect request", content = @Content)
    })
    @PostMapping(value = "/token-create")
    public void tokenCreate(@RequestParam @Email @NotEmpty @Valid String email) {
        if (appUserService.findByEmail(email).isPresent()) {
            eventPublisher.publishEvent(new OnPasswordResetTokenEmailEvent(email, LocaleContextHolder.getLocale()));
        }
    }

    @Operation(summary = "Validate if reset token was used or expired")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Password reset token is valid"),
            @ApiResponse(responseCode = "400", description = "Incorrect request", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthenticated: The token has expired", content = @Content),
            @ApiResponse(responseCode = "404", description = "The token was not found", content = @Content)
    })
    @GetMapping(value = "/token-validate")
    public void validateToken(@RequestParam @NotEmpty @Valid String token) throws NotFoundException, PasswordResetTokenExpiredException {
        passwordService.validate(token);
    }

    @Operation(summary = "Reset password")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Password reset was successful"),
            @ApiResponse(responseCode = "400", description = "Incorrect request", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthenticated: The token has expired", content = @Content),
            @ApiResponse(responseCode = "404", description = "The token was not found", content = @Content)
    })
    @PostMapping(value = "/password-reset", consumes = APPLICATION_JSON_VALUE)
    public void resetPassword(@RequestBody @Valid PasswordResetRequest request)
            throws NotFoundException, PasswordResetTokenExpiredException {
        passwordService.resetPassword(request);
    }

    @Operation(summary = "Change password")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Password change was successful"),
            @ApiResponse(responseCode = "400", description = "Incorrect request: Passwords were incorrect", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthenticated", content = @Content),
            @ApiResponse(responseCode = "404", description = "The user was not found", content = @Content)
    })
    @PostMapping(value = "/password-change", consumes = APPLICATION_JSON_VALUE)
    public void changePassword(@RequestBody @Valid PasswordChangeRequest request) throws NotFoundException, BadRequestException {
        AppUserDetails appUserDetails = AppUserDetailsSupplier.get();
        passwordService.changePassword(request, appUserDetails.getUsername());
    }
}
