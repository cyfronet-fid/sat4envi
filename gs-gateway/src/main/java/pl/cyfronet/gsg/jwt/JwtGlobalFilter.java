/*
 * Copyright 2020 ACC Cyfronet AGH
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

package pl.cyfronet.gsg.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@RequiredArgsConstructor
@Slf4j
public class JwtGlobalFilter implements GlobalFilter, Ordered {
    public static final String TOKEN_PREFIX = "Bearer ";
    public static final String LICENSED_LAYERS_ATTR = "Licensed-Layers";
    public static final String LAYERS_CLAIM = "layers";
    public static final String TOKEN_COOKIE = "token";

    private final JwtParser jwtParser;

    @Override
    public int getOrder() {
        // Must be before the LayerSecurityGatewayFilter
        return -1;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String token = getToken(exchange);
        if (token == null) {
            return chain.filter(exchange);
        }

        Set<String> licensedLayers = getLicensedLayers(token);
        if (licensedLayers == null) {
            return chain.filter(exchange);
        }

        exchange.getAttributes().put(LICENSED_LAYERS_ATTR, licensedLayers);
        return chain.filter(exchange);
    }

    private String getToken(ServerWebExchange exchange) {
        val request = exchange.getRequest();

        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader != null && authHeader.startsWith(TOKEN_PREFIX)) {
            return authHeader.substring(TOKEN_PREFIX.length());
        }

        HttpCookie authCookie = request.getCookies().getFirst(TOKEN_COOKIE);
        if (authCookie != null) {
            return authCookie.getValue();
        }
        return null;
    }

    private Set<String> getLicensedLayers(String token) {
        Jws<Claims> jws;
        try {
            jws = jwtParser.parseClaimsJws(token);
        } catch (Exception e) {
            log.debug("Cannot parse provided JWT token", e);
            return null;
        }
        try {
            // ObjectMapper by default maps layers to an ArrayList, and JJWT doesn't provide out of the box conversion
            // to Set. So I do it manually here.
            Collection<String> out = jws.getBody().get(LAYERS_CLAIM, Collection.class);
            return new HashSet<>(out);
        } catch (Exception e) {
            return Set.of();
        }
    }
}
