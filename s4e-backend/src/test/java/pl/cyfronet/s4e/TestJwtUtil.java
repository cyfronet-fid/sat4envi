package pl.cyfronet.s4e;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.JacksonSerializer;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import pl.cyfronet.s4e.security.AppUserDetails;

import java.util.Date;
import java.util.stream.Collectors;

import static pl.cyfronet.s4e.SecurityConfig.JWT_KEY;
import static pl.cyfronet.s4e.security.JWTTokenService.AUTHORITIES_KEY;

public class TestJwtUtil {
    public static RequestPostProcessor jwtBearerToken(AppUserDetails appUserDetails, ObjectMapper objectMapper) {
        return mockRequest -> {
            mockRequest.addHeader("Authorization", "Bearer " + createToken(appUserDetails, objectMapper));
            return mockRequest;
        };
    }

    private static String createToken(AppUserDetails appUserDetails, ObjectMapper objectMapper) {
        Claims claims = Jwts.claims()
                .setSubject(appUserDetails.getUsername())
                .setExpiration(new Date(System.currentTimeMillis() + 1000L));
        claims.put(AUTHORITIES_KEY, appUserDetails.getAuthorities().stream()
                .map(ga -> ga.getAuthority())
                .collect(Collectors.toList()));
        return Jwts.builder()
                .setClaims(claims)
                .signWith(JWT_KEY)
                .serializeToJsonWith(new JacksonSerializer<>(objectMapper))
                .compact();
    }
}
