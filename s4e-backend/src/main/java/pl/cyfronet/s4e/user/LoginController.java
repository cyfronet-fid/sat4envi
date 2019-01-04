package pl.cyfronet.s4e.user;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.cyfronet.s4e.security.JWTTokenService;

import static pl.cyfronet.s4e.Constants.API_PREFIX;

@RestController
@RequestMapping(API_PREFIX)
@RequiredArgsConstructor
public class LoginController {
    private final AuthenticationProvider authenticationProvider;
    private final JWTTokenService jwtTokenService;

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest loginRequest) throws AuthenticationException {
        String email = loginRequest.getEmail() != null ? loginRequest.getEmail() : "";
        String password = loginRequest.getPassword() != null ? loginRequest.getPassword() : "";

        val token = new UsernamePasswordAuthenticationToken(email, password);
        val authToken = (UsernamePasswordAuthenticationToken) authenticationProvider.authenticate(token);

        String jws = jwtTokenService.generateClaimsJws(authToken.getName());

        return LoginResponse.builder()
                .email(authToken.getName())
                .token(jws)
                .build();
    }
}
