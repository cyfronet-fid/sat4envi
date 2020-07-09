package pl.cyfronet.s4e.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.jackson.io.JacksonDeserializer;
import io.jsonwebtoken.jackson.io.JacksonSerializer;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;

import java.security.KeyPair;
import java.util.Date;
import java.util.stream.Collectors;

import static pl.cyfronet.s4e.security.SecurityConstants.JWT_AUTHORITIES_CLAIM;

@RequiredArgsConstructor
public class JwtTokenService {
    private final long expirationTime;

    private final ObjectMapper objectMapper;
    private final KeyPair jwtKeyPair;

    public Jws<Claims> parseClaimsJws(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(jwtKeyPair.getPublic())
                .deserializeJsonWith(new JacksonDeserializer<>(objectMapper))
                .build()
                .parseClaimsJws(token);
    }

    public String generateClaimsJws(Authentication auth) {
        Claims claims = Jwts.claims()
                .setSubject(auth.getName())
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime));
        claims.put(JWT_AUTHORITIES_CLAIM, auth.getAuthorities().stream()
                .map(ga -> ga.getAuthority())
                .collect(Collectors.toList()));
        return Jwts.builder()
                .setClaims(claims)
                .signWith(jwtKeyPair.getPrivate())
                .serializeToJsonWith(new JacksonSerializer<>(objectMapper))
                .compact();
    }
}
