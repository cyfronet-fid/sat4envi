package pl.cyfronet.s4e.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.jackson.io.JacksonDeserializer;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;

import java.util.Date;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static pl.cyfronet.s4e.TestJwtUtil.JWT_KEY_PAIR;

class JwtTokenServiceTest {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void shouldGenerateClaims() {
        JwtTokenService service = new JwtTokenService(10000L, objectMapper, JWT_KEY_PAIR);

        String[] authorities = new String[]{
                "ROLE_1",
                "OP_CREATE_STH"
        };
        val token = new TestingAuthenticationToken("test", null, authorities);

        String jws = service.generateClaimsJws(token);

        Jws<Claims> jwsClaims = Jwts.parserBuilder()
                .setSigningKey(JWT_KEY_PAIR.getPublic())
                .deserializeJsonWith(new JacksonDeserializer<>(objectMapper))
                .build()
                .parseClaimsJws(jws);

        assertThat(jwsClaims.getBody().getSubject(), is("test"));
        assertThat(jwsClaims.getBody().getExpiration(), greaterThan(new Date()));
        List<String> jwsAuthorities = jwsClaims.getBody().get(SecurityConstants.JWT_AUTHORITIES_CLAIM, List.class);
        assertThat(jwsAuthorities, contains("ROLE_1", "OP_CREATE_STH"));
    }

}
