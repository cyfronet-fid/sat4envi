package pl.cyfronet.s4e.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.jackson.io.JacksonDeserializer;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import pl.cyfronet.s4e.ex.NotFoundException;
import pl.cyfronet.s4e.util.LicenseHelper;

import java.util.Date;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static pl.cyfronet.s4e.TestJwtUtil.JWT_KEY_PAIR;

class JwtTokenServiceTest {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void shouldGenerateClaims() throws NotFoundException {
        LicenseHelper licenseHelper = mock(LicenseHelper.class);
        when(licenseHelper.readLicenseAuthorityToLayerName(new SimpleGrantedAuthority("LICENSE_READ_123")))
                .thenReturn("development:layer_123");
        JwtTokenService service = new JwtTokenService(10000L, objectMapper, JWT_KEY_PAIR, licenseHelper);

        String[] authorities = new String[]{
                "ROLE_1",
                "OP_CREATE_STH",
                "LICENSE_READ_123"
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
        assertThat(jwsAuthorities, containsInAnyOrder("ROLE_1", "OP_CREATE_STH", "LICENSE_READ_123"));
        List<String> layers = jwsClaims.getBody().get(SecurityConstants.JWT_LAYERS_CLAIM, List.class);
        assertThat(layers, contains("development:layer_123"));
    }

}
