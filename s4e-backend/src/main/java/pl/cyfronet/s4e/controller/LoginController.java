package pl.cyfronet.s4e.controller;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.http.ResponseEntity;
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

import static pl.cyfronet.s4e.Constants.API_PREFIX_V1;

@RestController
@RequestMapping(API_PREFIX_V1)
@RequiredArgsConstructor
public class LoginController {
    private final AuthenticationProvider authenticationProvider;
    private final JWTTokenService jwtTokenService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody @Valid LoginRequest loginRequest) throws AuthenticationException {
        val token = new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword());
        val authToken = (UsernamePasswordAuthenticationToken) authenticationProvider.authenticate(token);

        String jws = jwtTokenService.generateClaimsJws(authToken.getName());

        return ResponseEntity.ok(
                LoginResponse.builder()
                        .email(authToken.getName())
                        .token(jws)
                        .build());
    }
}
