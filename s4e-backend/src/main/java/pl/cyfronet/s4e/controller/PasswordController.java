package pl.cyfronet.s4e.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pl.cyfronet.s4e.controller.request.PasswordResetRequest;
import pl.cyfronet.s4e.event.OnPasswordResetTokenEmailEvent;
import pl.cyfronet.s4e.ex.BadRequestException;
import pl.cyfronet.s4e.ex.NotFoundException;
import pl.cyfronet.s4e.ex.PasswordResetTokenExpiredException;
import pl.cyfronet.s4e.service.AppUserService;
import pl.cyfronet.s4e.service.PasswordService;

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
            @ApiResponse(responseCode = "200", description = "Email with a password reset token was sent provided user with it existed")
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
            @ApiResponse(responseCode = "401", description = "The token has expired"),
            @ApiResponse(responseCode = "404", description = "The token was not found")
    })
    @GetMapping(value = "/token-validate")
    public void validateToken(@RequestParam @NotEmpty @Valid String token) throws NotFoundException, PasswordResetTokenExpiredException {
        passwordService.validate(token);
    }

    @Operation(summary = "Reset password")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Password reset was successful"),
            @ApiResponse(responseCode = "404", description = "The token was not found")
    })
    @PostMapping(value = "/password-reset", consumes = APPLICATION_JSON_VALUE)
    public void resetPassword(@RequestBody @Valid PasswordResetRequest passwordReset, @RequestParam("token") String token) throws NotFoundException {
        passwordService.resetPassword(passwordReset, token);
    }

    @Operation(summary = "Change password")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Password change was successful"),
            @ApiResponse(responseCode = "400", description = "Passwords were incorrect"),
            @ApiResponse(responseCode = "404", description = "The user was not found")
    })
    @PostMapping(value = "/password-change", consumes = APPLICATION_JSON_VALUE)
    @PreAuthorize("!isAnonymous()")
    public void changePassword(@RequestBody @Valid PasswordResetRequest passwordReset) throws NotFoundException, BadRequestException {
        passwordService.changePassword(passwordReset);
    }
}
