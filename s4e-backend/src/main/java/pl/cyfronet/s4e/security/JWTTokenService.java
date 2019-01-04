package pl.cyfronet.s4e.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.JacksonDeserializer;
import io.jsonwebtoken.io.JacksonSerializer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Date;

import static pl.cyfronet.s4e.SecurityConfig.JWT_KEY;

@Service
@RequiredArgsConstructor
public class JWTTokenService {
    private static final long EXPIRATION_TIME = Duration.ofHours(1).toMillis();

    private final ObjectMapper objectMapper;

    public Jws<Claims> parseClaimsJws(String token) {
         return Jwts.parser()
                .setSigningKey(JWT_KEY)
                .deserializeJsonWith(new JacksonDeserializer<>(objectMapper))
                .parseClaimsJws(token);
    }

    public String generateClaimsJws(String subject) {
        return Jwts.builder()
                .setSubject(subject)
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(JWT_KEY)
                .serializeToJsonWith(new JacksonSerializer<>(objectMapper))
                .compact();
    }
}
