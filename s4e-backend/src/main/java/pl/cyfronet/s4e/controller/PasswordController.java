package pl.cyfronet.s4e.controller;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
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
public class PasswordController {
    private final AppUserService appUserService;
    private final PasswordService passwordService;
    private final ApplicationEventPublisher eventPublisher;
    private final PasswordEncoder passwordEncoder;

    @ApiOperation("Create and send a password reset token based on email")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Email with a password reset token was sent provided user with it existed")
    })
    @PostMapping(value = "/token-create")
    public ResponseEntity<?> resetPassword(@RequestParam @Email @NotEmpty @Valid String email) {
        val optionalAppUser = appUserService.findByEmail(email);
        if (optionalAppUser.isPresent()) {
            eventPublisher.publishEvent(new OnPasswordResetTokenEmailEvent(optionalAppUser.get(), LocaleContextHolder.getLocale()));
        }
        return ResponseEntity.ok().build();
    }

    @ApiOperation("Validate if reset token was used or expired")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Password reset token is valid"),
            @ApiResponse(code = 401, message = "The token has expired"),
            @ApiResponse(code = 404, message = "The token was not found")
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

    @ApiOperation("Reset password")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Password reset was successful"),
            @ApiResponse(code = 404, message = "The token was not found")
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

    @ApiOperation("Change password")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Password change was successful"),
            @ApiResponse(code = 400, message = "Passwords were incorrect"),
            @ApiResponse(code = 404, message = "The user was not found")
    })
    @PostMapping(value = "/password-change")
    @ResponseBody
    @PreAuthorize("!isAnonymous()")
    public ResponseEntity<?> changePassword(@RequestBody @Valid PasswordResetRequest passwordReset) throws NotFoundException, BadRequestException {
        val appUser = appUserService.findByEmail(
                ((AppUserDetails) SecurityContextHolder.getContext().getAuthentication().getDetails()).getAppUser().getEmail())
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
