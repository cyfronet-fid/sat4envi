package pl.cyfronet.s4e.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.jackson.io.JacksonDeserializer;
import io.jsonwebtoken.jackson.io.JacksonSerializer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import pl.cyfronet.s4e.ex.NotFoundException;
import pl.cyfronet.s4e.util.LicenseHelper;

import java.security.KeyPair;
import java.util.*;
import java.util.stream.Collectors;

import static pl.cyfronet.s4e.security.SecurityConstants.*;

@RequiredArgsConstructor
@Slf4j
public class JwtTokenService {
    private final long expirationTime;

    private final ObjectMapper objectMapper;
    private final KeyPair jwtKeyPair;
    private final LicenseHelper licenseHelper;

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
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet()));
        claims.put(JWT_LAYERS_CLAIM, getUniqueLayers(auth.getAuthorities()));
        claims.put(JWT_PRIORITY_ACCESS_CLAIM, auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_MEMBER_ZK")));
        return Jwts.builder()
                .setClaims(claims)
                .signWith(jwtKeyPair.getPrivate())
                .serializeToJsonWith(new JacksonSerializer<>(objectMapper))
                .compact();
    }

    private Set<String> getUniqueLayers(Collection<? extends GrantedAuthority> authorities) {
        val out = new HashSet<String>();
        for (GrantedAuthority authority : authorities) {
            try {
                String layerName = licenseHelper.readLicenseAuthorityToLayerName(authority);
                if (layerName != null) {
                    out.add(layerName);
                }
            } catch (NotFoundException | NumberFormatException e) {
                log.warn("Incorrect LICENSE_READ_ authority: " + authority.getAuthority(), e);
            }
        }
        return out;
    }
}
