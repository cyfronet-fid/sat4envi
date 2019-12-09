package pl.cyfronet.s4e.controller;

import com.github.mkopylec.recaptcha.validation.ErrorCode;
import com.github.mkopylec.recaptcha.validation.RecaptchaValidationException;
import com.github.mkopylec.recaptcha.validation.RecaptchaValidator;
import com.github.mkopylec.recaptcha.validation.ValidationResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
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
import pl.cyfronet.s4e.controller.request.CreateUserWithGroupsRequest;
import pl.cyfronet.s4e.controller.request.RegisterRequest;
import pl.cyfronet.s4e.controller.request.UpdateUserGroupsRequest;
import pl.cyfronet.s4e.controller.response.AppUserResponse;
import pl.cyfronet.s4e.event.OnEmailConfirmedEvent;
import pl.cyfronet.s4e.event.OnRegistrationCompleteEvent;
import pl.cyfronet.s4e.event.OnResendRegistrationTokenEvent;
import pl.cyfronet.s4e.ex.AppUserCreationException;
import pl.cyfronet.s4e.ex.NotFoundException;
import pl.cyfronet.s4e.ex.RecaptchaException;
import pl.cyfronet.s4e.ex.RegistrationTokenExpiredException;
import pl.cyfronet.s4e.security.AppUserDetails;
import pl.cyfronet.s4e.service.AppUserService;
import pl.cyfronet.s4e.service.EmailVerificationService;
import pl.cyfronet.s4e.service.GroupService;

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
@Tag(name = "app-user", description = "The AppUser API")
public class AppUserController {
    private final AppUserService appUserService;
    private final EmailVerificationService emailVerificationService;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationEventPublisher eventPublisher;
    private final RecaptchaValidator recaptchaValidator;
    private final GroupService groupService;

    @Operation(summary = "Register a new user")
    @Parameters({
            @Parameter(name = "g-recaptcha-response", in = ParameterIn.QUERY, required = true)
    })
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "If user was registered. But also, when the username was taken and the registration didn't succeed"),
            @ApiResponse(responseCode = "400", description = "The request was not valid or recaptcha failed")
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
                .enabled(false)
                .build());

        eventPublisher.publishEvent(new OnRegistrationCompleteEvent(appUser, LocaleContextHolder.getLocale()));

        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Resend an email verification token based on email")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Email with a new email verification token was sent provided user with it existed and wasn't activated yet")
    })
    @PostMapping("/resend-registration-token-by-email")
    public ResponseEntity<?> resendRegistrationTokenByEmail(@RequestParam @Email @NotEmpty @Valid String email) {
        val optionalAppUser = appUserService.findByEmail(email);

        if (optionalAppUser.isPresent()) {
            eventPublisher.publishEvent(new OnResendRegistrationTokenEvent(optionalAppUser.get(), LocaleContextHolder.getLocale()));
        }

        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Resend registration token based on token")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Email with a new registration token was resent"),
            @ApiResponse(responseCode = "404", description = "The token was not found")
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

    @Operation(summary = "Confirm user email")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User account was activated"),
            @ApiResponse(responseCode = "401", description = "The token has expired"),
            @ApiResponse(responseCode = "404", description = "The token wasn't found")
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

    @Operation(summary = "Add user to an institution")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User was added"),
            @ApiResponse(responseCode = "403", description = "Forbidden: Don't have permission to add user"),
            @ApiResponse(responseCode = "404", description = "User or Group not found")
    })
    @PostMapping("/institutions/{institution}/users")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> addUserToInstitution(@RequestBody @Valid CreateUserWithGroupsRequest request,
                                                  @PathVariable("institution") String institutionSlug) throws AppUserCreationException, NotFoundException {
        appUserService.createFromRequest(request, institutionSlug);
//        eventPublisher.publishEvent(new OnRegistrationViaInstitutionCompleteEvent(appUser, LocaleContextHolder.getLocale()));
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Update user groups in an institution")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User groups were updated"),
            @ApiResponse(responseCode = "400", description = "Group not updated"),
            @ApiResponse(responseCode = "403", description = "Forbidden: Don't have permission to update user groups")
    })
    @PutMapping("/institutions/{institution}/users")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> updateUserGroupsInInstitution(@RequestBody @Valid UpdateUserGroupsRequest request,
                                                           @PathVariable("institution") String institutionSlug)
            throws NotFoundException {
        if (request.getGroupSlugs() != null) {
            groupService.updateUserGroups(request, institutionSlug);
        }
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Get user profile")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User was retrieved"),
            @ApiResponse(responseCode = "403", description = "Forbidden: Don't have permission to get profile"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/users/me")
    @PreAuthorize("isAuthenticated()")
    public AppUserResponse getMe() throws NotFoundException {
        AppUserDetails appUserDetails = (AppUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        AppUser appUser = appUserService.findByEmailWithRolesAndGroupsAndInstitution(appUserDetails.getUsername())
                .orElseThrow(() -> new NotFoundException("User not found for email: '" + appUserDetails.getUsername() + "'"));
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
