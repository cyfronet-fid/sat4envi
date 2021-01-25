/*
 * Copyright 2021 ACC Cyfronet AGH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

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
                .thenReturn("main:layer_123");
        JwtTokenService service = new JwtTokenService(10000L, objectMapper, JWT_KEY_PAIR, licenseHelper);

        String[] authorities = new String[]{
                "ROLE_1",
                "OP_CREATE_STH",
                "LICENSE_READ_123",
                "ROLE_MEMBER_ZK"
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
        assertThat(jwsAuthorities, containsInAnyOrder(authorities));
        List<String> layers = jwsClaims.getBody().get(SecurityConstants.JWT_LAYERS_CLAIM, List.class);
        assertThat(layers, contains("main:layer_123"));
        val priorityAccess = jwsClaims.getBody().get(SecurityConstants.JWT_PRIORITY_ACCESS_CLAIM, Boolean.class);
        assertThat(priorityAccess, is(true));
    }
}
