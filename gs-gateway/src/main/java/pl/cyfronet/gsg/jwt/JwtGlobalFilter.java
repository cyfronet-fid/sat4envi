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

    private final JwtParser jwtParser;

    @Override
    public int getOrder() {
        // Must be before the LayerSecurityGatewayFilter
        return -1;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        Set<String> licensedLayers = getLicensedLayers(exchange);
        if (licensedLayers != null) {
            exchange.getAttributes().put(LICENSED_LAYERS_ATTR, licensedLayers);
        }
        return chain.filter(exchange);
    }

    private Set<String> getLicensedLayers(ServerWebExchange exchange) {
        val request = exchange.getRequest();
        String authorization = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authorization == null || !authorization.startsWith(TOKEN_PREFIX)) {
            return null;
        }
        String token = authorization.substring(TOKEN_PREFIX.length());
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
