/*
 * Copyright 2020 ACC Cyfronet AGH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package pl.cyfronet.s4e.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
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
import pl.cyfronet.s4e.controller.response.TokenResponse;
import pl.cyfronet.s4e.properties.JwtProperties;
import pl.cyfronet.s4e.security.JwtTokenService;
import pl.cyfronet.s4e.security.SecurityConstants;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static pl.cyfronet.s4e.Constants.API_PREFIX_V1;

@RestController
@RequestMapping(path = API_PREFIX_V1, produces = APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "auth", description = "The Authorization API")
public class AuthController {
    private final AuthenticationProvider authenticationProvider;
    private final JwtTokenService jwtTokenService;
    private final JwtProperties jwtProperties;

    @Operation(summary = "Get authorization token")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "Incorrect request", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthenticated: Incorrect credentials or account doesn't exist", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden: Account disabled (not activated)", content = @Content)
    })
    @PostMapping(value = "/token", consumes = APPLICATION_JSON_VALUE)
    public TokenResponse token(@RequestBody @Valid LoginRequest loginRequest) throws AuthenticationException {
        val token = new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword());
        val authToken = authenticationProvider.authenticate(token);

        String jws = jwtTokenService.generateClaimsJws(authToken);

        return TokenResponse.builder()
                .email(authToken.getName())
                .token(jws)
                .build();
    }

    @Operation(summary = "Login to the application")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "OK",
                    headers = @Header(name = "Set-Cookie", schema = @Schema(example = "token=abcdef; Domain=data.sat4envi.imgw.pl; Path=/; HttpOnly; Max-Age=1234"))
            ),
            @ApiResponse(responseCode = "400", description = "Incorrect request", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthenticated: Incorrect credentials or account doesn't exist", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden: Account disabled (not activated)", content = @Content)
    })
    @PostMapping(value = "/login", consumes = APPLICATION_JSON_VALUE)
    public void login(
            @RequestBody @Valid LoginRequest loginRequest,
            HttpServletRequest request,
            HttpServletResponse response
    ) throws AuthenticationException {
        val token = new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword());
        val authToken = authenticationProvider.authenticate(token);

        String domain = determineCookieDomain(request.getServerName());
        String jws = jwtTokenService.generateClaimsJws(authToken);

        response.addCookie(authCookie(jws, domain));
    }

    @Operation(summary = "Logout from the application")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "OK",
                    headers = @Header(name = "Set-Cookie", schema = @Schema(example = "token=; Domain=data.sat4envi.imgw.pl; Path=/; HttpOnly; Max-Age=0")))
    })
    @PostMapping(value = "/logout", consumes = APPLICATION_JSON_VALUE)
    public void logout(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws AuthenticationException {
        String domain = determineCookieDomain(request.getServerName());
        Cookie authCookie = authCookie(null, domain);
        authCookie.setMaxAge(0);

        response.addCookie(authCookie);
    }

    private String determineCookieDomain(String host) {
        String propertyValue = jwtProperties.getCookie().getDomain();
        if (propertyValue == null || propertyValue.isBlank()) {
            return host;
        } else {
            return propertyValue;
        }
    }

    private Cookie authCookie(String content, String domain) {
        Cookie authCookie = new Cookie(SecurityConstants.COOKIE_NAME, content);
        authCookie.setMaxAge(maxAge());
        authCookie.setHttpOnly(true);
        authCookie.setPath("/");
        authCookie.setSecure(true);
        authCookie.setDomain(domain);
        return authCookie;
    }

    private int maxAge() {
        return (int) jwtProperties.getToken().getExpirationTime().toSeconds();
    }
}
