package pl.cyfronet.s4e;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.jackson.io.JacksonSerializer;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import pl.cyfronet.s4e.bean.AppUser;
import pl.cyfronet.s4e.properties.JwtProperties;
import pl.cyfronet.s4e.security.AppUserDetails;
import pl.cyfronet.s4e.security.LoadKeyPair;

import java.security.*;
import java.util.Date;
import java.util.HashSet;
import java.util.stream.Collectors;

import static pl.cyfronet.s4e.security.JwtTokenService.AUTHORITIES_KEY;

public class TestJwtUtil {
    public static final KeyPair JWT_KEY_PAIR;
    static {
        try {
            JwtProperties jwtProperties = new JwtProperties();
            jwtProperties.setKeyStore("dev_key.p12");
            jwtProperties.setKeyStorePassword("dev_password");
            jwtProperties.setKeyAlias("1");
            jwtProperties.setKeyPassword("dev_password");

            JWT_KEY_PAIR = LoadKeyPair.loadKeyPair(jwtProperties);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    public static RequestPostProcessor jwtBearerToken(AppUser user, ObjectMapper objectMapper) {
        return jwtBearerToken(user, objectMapper, JWT_KEY_PAIR);
    }

    public static RequestPostProcessor jwtBearerToken(AppUser user, ObjectMapper objectMapper, KeyPair jwtKeyPair) {
        return mockRequest -> {
            mockRequest.addHeader("Authorization", "Bearer " + createToken(createAppUserDetails(user), objectMapper, jwtKeyPair));
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

    private static String createToken(AppUserDetails appUserDetails, ObjectMapper objectMapper, KeyPair jwtKeyPair) {
        Claims claims = Jwts.claims()
                .setSubject(appUserDetails.getUsername())
                .setExpiration(new Date(System.currentTimeMillis() + 10000L));
        claims.put(AUTHORITIES_KEY, appUserDetails.getAuthorities().stream()
                .map(ga -> ga.getAuthority())
                .collect(Collectors.toList()));
        return Jwts.builder()
                .setClaims(claims)
                .signWith(jwtKeyPair.getPrivate())
                .serializeToJsonWith(new JacksonSerializer<>(objectMapper))
                .compact();
    }
}
