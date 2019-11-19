package pl.cyfronet.s4e.controller;

import com.github.mkopylec.recaptcha.validation.ErrorCode;
import com.github.mkopylec.recaptcha.validation.RecaptchaValidationException;
import com.github.mkopylec.recaptcha.validation.RecaptchaValidator;
import com.github.mkopylec.recaptcha.validation.ValidationResult;
import io.swagger.annotations.*;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import pl.cyfronet.s4e.bean.AppRole;
import pl.cyfronet.s4e.bean.AppUser;
import pl.cyfronet.s4e.controller.request.CreateUserWithGroupsRequest;
import pl.cyfronet.s4e.controller.request.RegisterRequest;
import pl.cyfronet.s4e.controller.response.AppUserResponse;
import pl.cyfronet.s4e.event.OnEmailConfirmedEvent;
import pl.cyfronet.s4e.event.OnRegistrationCompleteEvent;
import pl.cyfronet.s4e.event.OnResendRegistrationTokenEvent;
import pl.cyfronet.s4e.ex.*;
import pl.cyfronet.s4e.security.AppUserDetails;
import pl.cyfronet.s4e.service.AppUserService;
import pl.cyfronet.s4e.service.EmailVerificationService;
import pl.cyfronet.s4e.service.GroupService;
import pl.cyfronet.s4e.service.InstitutionService;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import java.time.LocalDateTime;
import java.util.List;

import static pl.cyfronet.s4e.Constants.API_PREFIX_V1;

@RestController
@RequestMapping(API_PREFIX_V1)
@RequiredArgsConstructor
public class AppUserController {
    private final AppUserService appUserService;
    private final EmailVerificationService emailVerificationService;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationEventPublisher eventPublisher;
    private final RecaptchaValidator recaptchaValidator;
    private final InstitutionService institutionService;
    private final GroupService groupService;

    @ApiOperation("Register a new user")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "g-recaptcha-response", dataType = "string", paramType = "query", required = true)
    })
    @ApiResponses({
            @ApiResponse(code = 200, message = "If user was registered. But also, when the username was taken and the registration didn't succeed"),
            @ApiResponse(code = 400, message = "The request was not valid or recaptcha failed", examples = @Example({
                    @ExampleProperty(mediaType = "application/json", value =
                            "{\"email\":[\"musi być adresem e-mail\"]}\n" +
                                    "or\n" +
                                    "{\"recaptcha\":[\"invalid-input-response\"]}"
                    )
            }))
    })
    @PostMapping("/register")
    public ResponseEntity<?> register(
            @RequestBody @Valid RegisterRequest registerRequest,
            HttpServletRequest request
    ) throws AppUserCreationException, RecaptchaException {
        validateRecaptcha(request);

        AppUser appUser = appUserService.save(AppUser.builder()
                .name(registerRequest.getName())
                .surname(registerRequest.getSurname())
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
    public ResponseEntity<?> resendRegistrationTokenByToken(@RequestParam @NotEmpty @Valid String token
    ) throws NotFoundException {
        val optionalToken = emailVerificationService.findByToken(token);

        if (optionalToken.isPresent()) {
            eventPublisher.publishEvent(new OnResendRegistrationTokenEvent(optionalToken.get().getAppUser(), LocaleContextHolder.getLocale()));
            return ResponseEntity.ok().build();
        } else {
            throw new NotFoundException("Provided token '" + token + "' not found");
        }
    }

    @ApiOperation("Confirm user email")
    @ApiResponses({
            @ApiResponse(code = 200, message = "User account was activated"),
            @ApiResponse(code = 401, message = "The token has expired"),
            @ApiResponse(code = 404, message = "The token wasn't found")
    })
    @PostMapping("/confirm-email")
    public ResponseEntity<?> confirmEmail(@RequestParam @NotEmpty @Valid String token
    ) throws NotFoundException, AppUserCreationException, RegistrationTokenExpiredException {
        val verificationToken = emailVerificationService.findByToken(token)
                .orElseThrow(() -> new NotFoundException("Provided token '" + token + "' not found"));

        if (verificationToken.getExpiryTimestamp().isBefore(LocalDateTime.now())) {
            throw new RegistrationTokenExpiredException("Provided token '" + token + "' expired");
        }

        val appUser = verificationToken.getAppUser();
        appUser.setEnabled(true);
        appUserService.save(appUser);

        eventPublisher.publishEvent(new OnEmailConfirmedEvent(verificationToken, LocaleContextHolder.getLocale()));

        return ResponseEntity.ok().build();
    }

    @ApiOperation("Add user to an institution")
    @ApiResponses({
            @ApiResponse(code = 200, message = "User was added"),
            @ApiResponse(code = 403, message = "Forbidden: Don't have permission to add user")
    })
    @PostMapping("/institutions/{institution}/users")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> addUserToInstitution(@RequestBody @Valid CreateUserWithGroupsRequest request,
                                                  @PathVariable("institution") String institutionSlug) throws UserViaInstitutionCreationException {
        AppUser appUser = AppUser.builder()
                .name(request.getName())
                .surname(request.getSurname())
                .email(request.getEmail())
                // TODO: FIXME
                .password(passwordEncoder.encode("passTemporary"))
                .enabled(false)
                .build();

        appUserService.saveWithGroupUpdate(appUser, request.getGroupSlugs(), institutionSlug);
//        eventPublisher.publishEvent(new OnRegistrationViaInstitutionCompleteEvent(appUser, LocaleContextHolder.getLocale()));
        return ResponseEntity.ok().build();
    }

    @ApiOperation("Get user profile")
    @ApiResponses({
            @ApiResponse(code = 200, message = "User was retrieved"),
            @ApiResponse(code = 403, message = "Forbidden: Don't have permission to get profile")
    })
    @GetMapping("/users/me")
    @PreAuthorize("isAuthenticated()")
    public AppUserResponse getMe() {
        AppUserDetails appUserDetails = (AppUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        AppUser appUser = appUserDetails.getAppUser();
        return AppUserResponse.of(appUser);
    }

    private void validateRecaptcha(HttpServletRequest request) throws RecaptchaException {
        try {
            ValidationResult validationResult = recaptchaValidator.validate(request);
            if (validationResult.isFailure()) {
                throw new RecaptchaException("Cannot validate request", validationResult.getErrorCodes());
            }
        } catch (RecaptchaValidationException e) {
            throw new RecaptchaException("Connection exception", List.of(ErrorCode.VALIDATION_HTTP_ERROR), e);
        }
    }
}
