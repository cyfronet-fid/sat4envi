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
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.apache.tomcat.websocket.Constants.AUTHORIZATION_HEADER_NAME;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private static final String TOKEN_PREFIX = "Bearer ";

    private final UserDetailsService userDetailsService;
    private final JwtTokenService jwtTokenService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        String header = request.getHeader(SecurityConstants.HEADER_NAME);

        if (header != null && header.startsWith(TOKEN_PREFIX)) {
            val authentication = getAuthentication(request);
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        chain.doFilter(request, response);
    }

    private UsernamePasswordAuthenticationToken getAuthentication(HttpServletRequest request) {
        String token = request.getHeader(SecurityConstants.HEADER_NAME);

        if (token == null) {
            return null;
        }

        final String subject;
        try {
            subject = jwtTokenService.parseClaimsJws(token.substring(TOKEN_PREFIX.length()))
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
