package pl.cyfronet.s4e.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import pl.cyfronet.s4e.bean.AppUser;
import pl.cyfronet.s4e.controller.request.PasswordResetRequest;
import pl.cyfronet.s4e.event.OnPasswordResetTokenEmailEvent;
import pl.cyfronet.s4e.ex.BadRequestException;
import pl.cyfronet.s4e.ex.NotFoundException;
import pl.cyfronet.s4e.ex.PasswordResetTokenExpiredException;
import pl.cyfronet.s4e.security.AppUserDetails;
import pl.cyfronet.s4e.service.AppUserService;
import pl.cyfronet.s4e.service.PasswordService;

import javax.validation.Valid;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import java.time.LocalDateTime;

import static pl.cyfronet.s4e.Constants.API_PREFIX_V1;

@RestController
@RequestMapping(API_PREFIX_V1)
@RequiredArgsConstructor
@Tag(name = "password", description = "The Password API")
public class PasswordController {
    private final AppUserService appUserService;
    private final PasswordService passwordService;
    private final ApplicationEventPublisher eventPublisher;
    private final PasswordEncoder passwordEncoder;

    @Operation(summary = "Create and send a password reset token based on email")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Email with a password reset token was sent provided user with it existed")
    })
    @PostMapping(value = "/token-create")
    public ResponseEntity<?> resetPassword(@RequestParam @Email @NotEmpty @Valid String email) {
        val optionalAppUser = appUserService.findByEmail(email);
        if (optionalAppUser.isPresent()) {
            eventPublisher.publishEvent(new OnPasswordResetTokenEmailEvent(optionalAppUser.get(), LocaleContextHolder.getLocale()));
        }
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Validate if reset token was used or expired")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Password reset token is valid"),
            @ApiResponse(responseCode = "401", description = "The token has expired"),
            @ApiResponse(responseCode = "404", description = "The token was not found")
    })
    @GetMapping(value = "/token-validate")
    public ResponseEntity<?> validateToken(@RequestParam @NotEmpty @Valid String token) throws NotFoundException, PasswordResetTokenExpiredException {
        val resetPasswordToken = passwordService.findByToken(token)
                .orElseThrow(() -> new NotFoundException("Provided token '" + token + "' not found"));

        if (resetPasswordToken.getExpiryTimestamp().isBefore(LocalDateTime.now())) {
            throw new PasswordResetTokenExpiredException("Provided token '" + token + "' expired");
        }

        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Reset password")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Password reset was successful"),
            @ApiResponse(responseCode = "404", description = "The token was not found")
    })
    @PostMapping(value = "/password-reset")
    @ResponseBody
    public ResponseEntity<?> resetPassword(@RequestBody @Valid PasswordResetRequest passwordReset, @RequestParam("token") String token) throws NotFoundException {
        val resetPasswordToken = passwordService.findByToken(token)
                .orElseThrow(() -> new NotFoundException("Provided token '" + token + "' not found"));
        AppUser appUser = resetPasswordToken.getAppUser();
        appUser.setPassword(passwordEncoder.encode(passwordReset.getNewPassword()));
        appUserService.update(appUser);
        passwordService.delete(resetPasswordToken.getId());

        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Change password")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Password change was successful"),
            @ApiResponse(responseCode = "400", description = "Passwords were incorrect"),
            @ApiResponse(responseCode = "404", description = "The user was not found")
    })
    @PostMapping(value = "/password-change")
    @ResponseBody
    @PreAuthorize("!isAnonymous()")
    public ResponseEntity<?> changePassword(@RequestBody @Valid PasswordResetRequest passwordReset) throws NotFoundException, BadRequestException {
        val appUser = appUserService.findByEmail(
                ((AppUserDetails) SecurityContextHolder.getContext().getAuthentication().getDetails()).getEmail())
                .orElseThrow(() -> new NotFoundException());
        if (appUser.getPassword().equals(passwordEncoder.encode(passwordReset.getOldPassword()))) {
            appUser.setPassword(passwordEncoder.encode(passwordReset.getNewPassword()));
            appUserService.update(appUser);
        } else {
            throw new BadRequestException("Provided passwords were incorrect");
        }

        return ResponseEntity.ok().build();
    }
}
