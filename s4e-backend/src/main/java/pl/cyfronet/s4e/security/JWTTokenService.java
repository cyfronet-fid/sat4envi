package pl.cyfronet.s4e.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.JacksonDeserializer;
import io.jsonwebtoken.io.JacksonSerializer;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Date;
import java.util.stream.Collectors;

import static pl.cyfronet.s4e.SecurityConfig.JWT_KEY;

@Service
@RequiredArgsConstructor
public class JWTTokenService {
    /// Half a day. In case of an 8h shift require a login every day.
    public static final long EXPIRATION_TIME = Duration.ofHours(12).toMillis();
    public static final String AUTHORITIES_KEY = "authorities";

    private final ObjectMapper objectMapper;

    public Jws<Claims> parseClaimsJws(String token) {
         return Jwts.parser()
                .setSigningKey(JWT_KEY)
                .deserializeJsonWith(new JacksonDeserializer<>(objectMapper))
                .parseClaimsJws(token);
    }

    public String generateClaimsJws(Authentication auth) {
        Claims claims = Jwts.claims()
                .setSubject(auth.getName())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME));
        claims.put(AUTHORITIES_KEY, auth.getAuthorities().stream()
                .map(ga -> ga.getAuthority())
                .collect(Collectors.toList()));
        return Jwts.builder()
                .setClaims(claims)
                .signWith(JWT_KEY)
                .serializeToJsonWith(new JacksonSerializer<>(objectMapper))
                .compact();
    }
}
