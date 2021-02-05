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

package pl.cyfronet.s4e;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.jackson.io.JacksonSerializer;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import pl.cyfronet.s4e.bean.AppUser;
import pl.cyfronet.s4e.properties.JwtProperties;
import pl.cyfronet.s4e.security.AppUserDetails;
import pl.cyfronet.s4e.security.LoadKeyPair;
import pl.cyfronet.s4e.security.SecurityConstants;

import javax.servlet.http.Cookie;
import java.security.KeyPair;
import java.util.Date;
import java.util.stream.Collectors;

import static pl.cyfronet.s4e.security.SecurityConstants.JWT_AUTHORITIES_CLAIM;

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
            if (user != null) {
                mockRequest.addHeader(
                        SecurityConstants.HEADER_NAME,
                        "Bearer " + createToken(createAppUserDetails(user), objectMapper, jwtKeyPair)
                );
            }

            return mockRequest;
        };
    }

    public static RequestPostProcessor jwtCookieToken(AppUser user, ObjectMapper objectMapper) {
        return mockRequest -> {
            String token = createToken(createAppUserDetails(user), objectMapper, JWT_KEY_PAIR);
            Cookie cookie = new Cookie(SecurityConstants.COOKIE_NAME, token);

            Cookie[] cookies = mockRequest.getCookies();
            Cookie[] updatedCookies = ArrayUtils.add(cookies, cookie);
            mockRequest.setCookies(updatedCookies);

            return mockRequest;
        };
    }

    private static AppUserDetails createAppUserDetails(AppUser user) {
        return new AppUserDetails(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getSurname(),
                user.getAuthorities().stream()
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toUnmodifiableSet()),
                user.getPassword(),
                user.isEnabled());
    }

    private static String createToken(AppUserDetails appUserDetails, ObjectMapper objectMapper, KeyPair jwtKeyPair) {
        Claims claims = Jwts.claims()
                .setSubject(appUserDetails.getUsername())
                .setExpiration(new Date(System.currentTimeMillis() + 10000L));
        claims.put(JWT_AUTHORITIES_CLAIM, appUserDetails.getAuthorities().stream()
                .map(ga -> ga.getAuthority())
                .collect(Collectors.toList()));
        return Jwts.builder()
                .setClaims(claims)
                .signWith(jwtKeyPair.getPrivate())
                .serializeToJsonWith(new JacksonSerializer<>(objectMapper))
                .compact();
    }
}
