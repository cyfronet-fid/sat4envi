package pl.cyfronet.s4e.controller;

import com.github.mkopylec.recaptcha.validation.ErrorCode;
import com.github.mkopylec.recaptcha.validation.RecaptchaValidationException;
import com.github.mkopylec.recaptcha.validation.RecaptchaValidator;
import com.github.mkopylec.recaptcha.validation.ValidationResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
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
import pl.cyfronet.s4e.service.GroupService;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static pl.cyfronet.s4e.Constants.API_PREFIX_V1;

@RestController
@RequestMapping(path = API_PREFIX_V1, produces = APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "app-user", description = "The AppUser API")
public class AppUserController {
    private final AppUserService appUserService;
    private final RecaptchaValidator recaptchaValidator;
    private final GroupService groupService;
    private final ApplicationEventPublisher eventPublisher;

    @Operation(summary = "Register a new user")
    @Parameters({
            @Parameter(name = "g-recaptcha-response", in = ParameterIn.QUERY, required = true)
    })
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "If user was registered. But also, when the username was taken and the registration didn't succeed"),
            @ApiResponse(responseCode = "400", description = "The request was not valid or recaptcha failed")
    })
    @PostMapping(value = "/register", consumes = APPLICATION_JSON_VALUE)
    public void register(
            @RequestBody @Valid RegisterRequest registerRequest,
            HttpServletRequest request
    ) throws AppUserCreationException, RecaptchaException {
        validateRecaptcha(request);
        appUserService.register(registerRequest);
        eventPublisher.publishEvent(new OnRegistrationCompleteEvent(registerRequest.getEmail(), LocaleContextHolder.getLocale()));
    }

    @Operation(summary = "Resend an email verification token based on email")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Email with a new email verification token was sent provided user with it existed and wasn't activated yet"),
            @ApiResponse(responseCode = "400", description = "Incorrect request", content = @Content)
    })
    @PostMapping("/resend-registration-token-by-email")
    public void resendRegistrationTokenByEmail(@RequestParam @Email @NotEmpty @Valid String email) {
        if (appUserService.findByEmail(email).isPresent()) {
            eventPublisher.publishEvent(new OnResendRegistrationTokenEvent(email, LocaleContextHolder.getLocale()));
        }
    }

    @Operation(summary = "Resend registration token based on token")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Email with a new registration token was resent"),
            @ApiResponse(responseCode = "400", description = "Incorrect request", content = @Content),
            @ApiResponse(responseCode = "404", description = "Not found", content = @Content)
    })
    @PostMapping("/resend-registration-token-by-token")
    public void resendRegistrationTokenByToken(@RequestParam @NotEmpty @Valid String token
    ) throws NotFoundException {
        String requesterEmail = appUserService.getRequesterEmailBy(token);
        eventPublisher.publishEvent(new OnResendRegistrationTokenEvent(requesterEmail, LocaleContextHolder.getLocale()));
    }

    @Operation(summary = "Confirm user email")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User account was activated"),
            @ApiResponse(responseCode = "400", description = "Incorrect request", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthenticated: The token has expired", content = @Content),
            @ApiResponse(responseCode = "404", description = "Not found", content = @Content)
    })
    @PostMapping("/confirm-email")
    public void confirmEmail(@RequestParam @NotEmpty @Valid String token
    ) throws NotFoundException, RegistrationTokenExpiredException {
        Long emailVerificationId = appUserService.confirmEmail(token).getId();
        String requesterEmail = appUserService.getRequesterEmailBy(token);
        eventPublisher.publishEvent(new OnEmailConfirmedEvent(requesterEmail, emailVerificationId, LocaleContextHolder.getLocale()));
    }

    @Operation(summary = "Add user to an institution")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User was added"),
            @ApiResponse(responseCode = "400", description = "Incorrect request", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthenticated", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content),
            @ApiResponse(responseCode = "404", description = "Not found", content = @Content)
    })
    @PostMapping(value = "/institutions/{institution}/users", consumes = APPLICATION_JSON_VALUE)
    @PreAuthorize("isAuthenticated() && isInstitutionAdmin(#institutionSlug)")
    public void addUserToInstitution(@RequestBody @Valid CreateUserWithGroupsRequest request,
                                                  @PathVariable("institution") String institutionSlug) throws AppUserCreationException, NotFoundException {
        appUserService.createFromRequest(request, institutionSlug);
//        eventPublisher.publishEvent(new OnRegistrationViaInstitutionCompleteEvent(appUser, LocaleContextHolder.getLocale()));
    }

    @Operation(summary = "Update user groups in an institution")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User groups were updated"),
            @ApiResponse(responseCode = "400", description = "Incorrect request", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthenticated", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content),
            @ApiResponse(responseCode = "404", description = "Not found", content = @Content)
    })
    @PutMapping(value = "/institutions/{institution}/users", consumes = APPLICATION_JSON_VALUE)
    @PreAuthorize("isAuthenticated() && isInstitutionAdmin(#institutionSlug)")
    public void updateUserGroupsInInstitution(@RequestBody @Valid UpdateUserGroupsRequest request,
                                                           @PathVariable("institution") String institutionSlug)
            throws NotFoundException {
        if (request.getGroupsWithRoles() != null) {
            groupService.updateUserGroups(request, institutionSlug);
        }
    }

    @Operation(summary = "Get user profile")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User was retrieved"),
            @ApiResponse(responseCode = "401", description = "Unauthenticated", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content),
            @ApiResponse(responseCode = "404", description = "Not found", content = @Content)
    })
    @GetMapping("/users/me")
    @PreAuthorize("isAuthenticated()")
    public AppUserResponse getMe() throws NotFoundException {
        AppUserDetails appUserDetails = (AppUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return appUserService.findByEmailWithRolesAndGroupsAndInstitution(appUserDetails.getUsername(), AppUserResponse.class)
                .orElseThrow(() -> new NotFoundException("User not found for email: '" + appUserDetails.getUsername() + "'"));
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
