package pl.cyfronet.s4e.controller;

import io.swagger.annotations.*;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import pl.cyfronet.s4e.bean.AppRole;
import pl.cyfronet.s4e.bean.AppUser;
import pl.cyfronet.s4e.controller.request.RegisterRequest;
import pl.cyfronet.s4e.event.OnEmailConfirmedEvent;
import pl.cyfronet.s4e.event.OnRegistrationCompleteEvent;
import pl.cyfronet.s4e.event.OnResendRegistrationTokenEvent;
import pl.cyfronet.s4e.ex.AppUserCreationException;
import pl.cyfronet.s4e.ex.NotFoundException;
import pl.cyfronet.s4e.ex.RegistrationTokenExpiredException;
import pl.cyfronet.s4e.service.AppUserService;
import pl.cyfronet.s4e.service.EmailVerificationService;

import javax.validation.Valid;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import java.time.LocalDateTime;

import static pl.cyfronet.s4e.Constants.API_PREFIX_V1;

@RestController
@RequestMapping(API_PREFIX_V1)
@RequiredArgsConstructor
public class AppUserController {
    private final AppUserService appUserService;
    private final EmailVerificationService emailVerificationService;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationEventPublisher eventPublisher;

    @ApiOperation("Register a new user")
    @ApiResponses({
            @ApiResponse(code = 200, message = "If user was registered. But also, when the username was taken and the registration didn't succeed"),
            @ApiResponse(code = 400, message = "The request was not valid", examples = @Example({
                    @ExampleProperty(mediaType = "application/json", value = "{\"email\":[\"musi być adresem e-mail\"]}")
            }))
    })
    @PostMapping("/register")
    public ResponseEntity<?> register(
            @RequestBody @Valid RegisterRequest registerRequest
    ) throws AppUserCreationException {
        AppUser appUser = appUserService.save(AppUser.builder()
                .email(registerRequest.getEmail())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                // for now it will be only possible to register as the lowest category
                .role(AppRole.CAT1)
                .enabled(false)
                .build());

        eventPublisher.publishEvent(new OnRegistrationCompleteEvent(appUser, LocaleContextHolder.getLocale()));

        return ResponseEntity.ok().build();
    }

    @ApiOperation("Resend an email verification token based on email")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Email with a new email verification token was sent provided user with it existed and wasn't activated yet")
    })
    @PostMapping("/resend-registration-token-by-email")
    public ResponseEntity<?> resendRegistrationTokenByEmail(@RequestParam @Email @NotEmpty @Valid String email) {
        val optionalAppUser = appUserService.findByEmail(email);

        if (optionalAppUser.isPresent()) {
            eventPublisher.publishEvent(new OnResendRegistrationTokenEvent(optionalAppUser.get(), LocaleContextHolder.getLocale()));
        }

        return ResponseEntity.ok().build();
    }

    @ApiOperation("Resend registration token based on token")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Email with a new registration token was resent"),
            @ApiResponse(code = 404, message = "The token was not found")
    })
    @PostMapping("/resend-registration-token-by-token")
    public ResponseEntity<?> resendRegistrationTokenByToken(@RequestParam @NotEmpty @Valid String token) throws NotFoundException {
        val optionalToken = emailVerificationService.findByToken(token);

        if (optionalToken.isPresent()) {
            eventPublisher.publishEvent(new OnResendRegistrationTokenEvent(optionalToken.get().getAppUser(), LocaleContextHolder.getLocale()));
            return ResponseEntity.ok().build();
        } else {
            throw new NotFoundException("Provided token '"+token+"' not found");
        }
    }

    @ApiOperation("Confirm user email")
    @ApiResponses({
            @ApiResponse(code = 200, message = "User account was activated"),
            @ApiResponse(code = 401, message = "The token has expired"),
            @ApiResponse(code = 404, message = "The token wasn't found")
    })
    @PostMapping("/confirm-email")
    public ResponseEntity<?> confirmEmail(@RequestParam @NotEmpty @Valid String token) throws NotFoundException, AppUserCreationException, RegistrationTokenExpiredException {
        val verificationToken = emailVerificationService.findByToken(token)
                .orElseThrow(() -> new NotFoundException("Provided token '"+token+"' not found"));

        if (verificationToken.getExpiryTimestamp().isBefore(LocalDateTime.now())) {
            throw new RegistrationTokenExpiredException("Provided token '"+token+"' expired");
        }

        val appUser = verificationToken.getAppUser();
        appUser.setEnabled(true);
        appUserService.save(appUser);

        eventPublisher.publishEvent(new OnEmailConfirmedEvent(verificationToken, LocaleContextHolder.getLocale()));

        return ResponseEntity.ok().build();
    }
}
