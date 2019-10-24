package pl.cyfronet.s4e.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.JacksonDeserializer;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;

import java.util.Date;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static pl.cyfronet.s4e.SecurityConfig.JWT_KEY;
import static pl.cyfronet.s4e.security.JWTTokenService.AUTHORITIES_KEY;

class JWTTokenServiceTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void shouldGenerateClaims() {
        JWTTokenService service = new JWTTokenService(objectMapper);

        String[] authorities = new String[] {
                "ROLE_1",
                "OP_CREATE_STH"
        };
        val token = new TestingAuthenticationToken("test", null, authorities);

        String jws = service.generateClaimsJws(token);

        Jws<Claims> jwsClaims = Jwts.parser()
                .setSigningKey(JWT_KEY)
                .deserializeJsonWith(new JacksonDeserializer<>(objectMapper))
                .parseClaimsJws(jws);

        assertThat(jwsClaims.getBody().getSubject(), is("test"));
        assertThat(jwsClaims.getBody().getExpiration().after(new Date()), is(true));
        List<String> jwsAuthorities = jwsClaims.getBody().get(AUTHORITIES_KEY, List.class);
        assertThat(jwsAuthorities, contains("ROLE_1", "OP_CREATE_STH"));
    }

}
