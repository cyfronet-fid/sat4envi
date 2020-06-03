package pl.cyfronet.gsg.security;

import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;
import pl.cyfronet.gsg.jwt.JwtGlobalFilter;
import reactor.core.publisher.Mono;

import java.time.Clock;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Map;
import java.util.Set;

@RequiredArgsConstructor
public class LayerSecurityGatewayFilter implements GatewayFilter {
    public static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

    private final Clock clock;

    private final String layersQueryParam;
    private final String timeQueryParam;

    private final Duration freshDuration;

    private final Map<String, AccessType> access;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String layer = request.getQueryParams().getFirst(layersQueryParam);
        if (layer == null) {
            return responseStatus(exchange, HttpStatus.BAD_REQUEST);
        }
        AccessType accessType = access.getOrDefault(layer, AccessType.PRIVATE);
        if (accessType == AccessType.OPEN) {
            return chain.filter(exchange);
        } else if (accessType == AccessType.EUMETSAT) {
            if (isFresh(request)) {
                return requiresLicense(exchange, chain, layer);
            } else {
                return chain.filter(exchange);
            }
        } else {
            return requiresLicense(exchange, chain, layer);
        }
    }

    private Mono<Void> responseStatus(ServerWebExchange exchange, HttpStatus status) {
        exchange.getResponse().setStatusCode(status);
        return exchange.getResponse().setComplete();
    }

    private boolean isFresh(ServerHttpRequest request) {
        String time = request.getQueryParams().getFirst(timeQueryParam);
        // The layers in GeoServer must be configured, so by default they serve the oldest scene,
        // so the EUMETSAT license won't apply.
        if (time == null) {
            return false;
        }
        ZonedDateTime dateTime;
        try {
            dateTime = ZonedDateTime.parse(time, TIME_FORMATTER);
        } catch (DateTimeParseException e) {
            // In case of exception here assume it's fresh.
            return true;
        }
        ZonedDateTime now = ZonedDateTime.now(clock);
        return dateTime.isAfter(now.minus(freshDuration));
    }

    private Mono<Void> requiresLicense(ServerWebExchange exchange, GatewayFilterChain chain, String layer) {
        Set<String> licensedLayers = exchange.getAttribute(JwtGlobalFilter.LICENSED_LAYERS_ATTR);
        if (licensedLayers != null) {
            if (licensedLayers.contains(layer)) {
                return chain.filter(exchange);
            } else {
                return responseStatus(exchange, HttpStatus.FORBIDDEN);
            }
        } else {
            return responseStatus(exchange, HttpStatus.UNAUTHORIZED);
        }
    }
}
