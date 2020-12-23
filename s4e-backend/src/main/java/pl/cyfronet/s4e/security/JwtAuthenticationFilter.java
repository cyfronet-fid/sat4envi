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

package pl.cyfronet.s4e.security;

import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Extracts Authorization from request.
 * Supports header and cookie.
 *
 * <p>
 * Authorization header takes precedence over token cookie.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private static final String TOKEN_PREFIX = "Bearer ";

    private final UserDetailsService userDetailsService;
    private final JwtTokenService jwtTokenService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        String token = getTokenFromHeader(request);
        if (token == null) {
            token = getTokenFromCookie(request);
        }

        if (token != null) {
            val authentication = getAuthentication(token);
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        chain.doFilter(request, response);
    }

    private String getTokenFromHeader(HttpServletRequest request) {
        String header = request.getHeader(SecurityConstants.HEADER_NAME);

        if (header != null && header.startsWith(TOKEN_PREFIX)) {
            return header.substring(TOKEN_PREFIX.length());
        } else {
            return null;
        }
    }

    private String getTokenFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie != null && SecurityConstants.COOKIE_NAME.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    private UsernamePasswordAuthenticationToken getAuthentication(String token) {
        final String subject;
        try {
            subject = jwtTokenService.parseClaimsJws(token)
                .getBody().getSubject();
        } catch (JwtException e) {
            log.debug("JWS verification failed", e);
            return null;
        }

        if (subject == null) {
            log.debug("JWS token without a subject: fail");
            return null;
        }

        final AppUserDetails appUserDetails;
        try {
            appUserDetails = (AppUserDetails) userDetailsService.loadUserByUsername(subject);
        } catch (UsernameNotFoundException e) {
            log.info("AppUser with email "+subject+" not found, even though a valid JWS was presented", e);
            return null;
        }

        val authToken = new UsernamePasswordAuthenticationToken(appUserDetails, null, appUserDetails.getAuthorities());
        authToken.setDetails(appUserDetails);
        return authToken;
    }
}
