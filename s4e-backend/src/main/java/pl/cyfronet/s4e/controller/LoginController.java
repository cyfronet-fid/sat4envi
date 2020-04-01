package pl.cyfronet.s4e.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.cyfronet.s4e.controller.request.LoginRequest;
import pl.cyfronet.s4e.controller.response.LoginResponse;
import pl.cyfronet.s4e.security.JWTTokenService;

import javax.validation.Valid;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static pl.cyfronet.s4e.Constants.API_PREFIX_V1;

@RestController
@RequestMapping(path = API_PREFIX_V1, produces = APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "login", description = "The Login API")
public class LoginController {
    private final AuthenticationProvider authenticationProvider;
    private final JWTTokenService jwtTokenService;

    @Operation(summary = "Login to the application")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "Incorrect request", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthenticated: Incorrect credentials or account doesn't exist", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden: Account disabled (not activated)", content = @Content)
    })
    @PostMapping(value = "/login", consumes = APPLICATION_JSON_VALUE)
    public LoginResponse login(@RequestBody @Valid LoginRequest loginRequest) throws AuthenticationException {
        val token = new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword());
        val authToken = authenticationProvider.authenticate(token);

        String jws = jwtTokenService.generateClaimsJws(authToken);

        return LoginResponse.builder()
                .email(authToken.getName())
                .token(jws)
                .build();
    }
}
