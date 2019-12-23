package pl.cyfronet.s4e;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.JacksonSerializer;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import pl.cyfronet.s4e.bean.AppUser;
import pl.cyfronet.s4e.security.AppUserDetails;

import java.util.Date;
import java.util.HashSet;
import java.util.stream.Collectors;

import static pl.cyfronet.s4e.SecurityConfig.JWT_KEY;
import static pl.cyfronet.s4e.security.JWTTokenService.AUTHORITIES_KEY;

public class TestJwtUtil {
    public static RequestPostProcessor jwtBearerToken(AppUser user, ObjectMapper objectMapper) {
        return mockRequest -> {
            mockRequest.addHeader("Authorization", "Bearer " + createToken(createAppUserDetails(user), objectMapper));
            return mockRequest;
        };
    }

    private static AppUserDetails createAppUserDetails(AppUser user) {
        return new AppUserDetails(
                user.getEmail(),
                user.getName(),
                user.getSurname(),
                new HashSet<>(),
                user.getPassword(),
                user.isEnabled(),
                user.isMemberZK(),
                user.isAdmin());
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
