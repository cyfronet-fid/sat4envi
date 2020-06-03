package pl.cyfronet.gsg.whitelist;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.Set;

import static org.springframework.util.CollectionUtils.unmodifiableMultiValueMap;

@RequiredArgsConstructor
public class WhitelistRequestParametersGatewayFilter implements GatewayFilter {
    private final Set<String> allowedParams;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        val filteredQueryParams = new LinkedMultiValueMap<String, String>();
        request.getQueryParams().forEach((key, values) -> {
            if (allowedParams.contains(key)) {
                filteredQueryParams.addAll(key, values);
            }
        });

        URI newUri = UriComponentsBuilder.fromUri(request.getURI())
                .replaceQueryParams(unmodifiableMultiValueMap(filteredQueryParams))
                .build(true)
                .toUri();

        ServerHttpRequest updatedRequest = exchange.getRequest().mutate()
                .uri(newUri)
                .build();

        return chain.filter(exchange.mutate().request(updatedRequest).build());
    }
}
