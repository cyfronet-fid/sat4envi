package pl.cyfronet.gsg;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.jackson.io.JacksonDeserializer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.OrderedGatewayFilter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import pl.cyfronet.gsg.jwt.JwtGlobalFilter;
import pl.cyfronet.gsg.security.AccessType;
import pl.cyfronet.gsg.security.LayerSecurityGatewayFilter;
import pl.cyfronet.gsg.whitelist.WhitelistRequestParametersGatewayFilter;

import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.time.Clock;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SpringBootConfiguration
@EnableAutoConfiguration
@EnableConfigurationProperties({JwtProperties.class, GatewayProperties.class})
public class GsGatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(GsGatewayApplication.class, args);
    }

    @Bean
    public PublicKey jwtSigningKey(JwtProperties jwtProperties) throws NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] bytes = jwtProperties.getPublicKey().getBytes(StandardCharsets.UTF_8);
        // See https://tools.ietf.org/html/rfc1421#section-4.3.2.4
        Base64.Decoder mimeDecoder = Base64.getMimeDecoder();
        byte[] decoded = mimeDecoder.decode(jwtProperties.getPublicKey());
        return KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(decoded));
    }

    @Bean
    public JwtParser jwtParser(PublicKey jwtSigningKey, ObjectMapper objectMapper) {
        return Jwts.parserBuilder()
                .setSigningKey(jwtSigningKey)
                .deserializeJsonWith(new JacksonDeserializer<>(objectMapper))
                .build();
    }

    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }

    @Bean
    public Map<String, AccessType> layerAccessType(GatewayProperties gatewayProperties) {
        Map<String, AccessType> layerAccessType = new HashMap<>();
        gatewayProperties.getOpenLayers().stream()
                .map(String::strip)
                .forEach(layer -> layerAccessType.put(layer, AccessType.OPEN));
        gatewayProperties.getEumetsatLayers().stream()
                .map(String::strip)
                .forEach(layer -> layerAccessType.put(layer, AccessType.EUMETSAT));
        return Map.copyOf(layerAccessType);
    }

    @Bean
    public GlobalFilter jwtGlobalFilter(JwtParser jwtParser) {
        return new JwtGlobalFilter(jwtParser);
    }

    @Bean
    public GatewayFilter whitelistRequestParametersGatewayFilter(GatewayProperties gatewayProperties) {
        GatewayFilter filter = new WhitelistRequestParametersGatewayFilter(gatewayProperties.getAllowedParams());
        return new OrderedGatewayFilter(filter, 0);
    }

    @Bean
    public GatewayFilter layerSecurityGatewayFilter(Clock clock, GatewayProperties gatewayProperties, Map<String, AccessType> layerAccessType) {
        GatewayFilter filter = new LayerSecurityGatewayFilter(
                clock,
                gatewayProperties.getQueryParams().getLayers(),
                gatewayProperties.getQueryParams().getTime(),
                gatewayProperties.getFreshDuration(),
                layerAccessType
        );
        return new OrderedGatewayFilter(filter, 1);
    }

    @Bean
    public RouteLocator customRouteLocator(GatewayProperties gatewayProperties, List<GatewayFilter> gatewayFilters, RouteLocatorBuilder builder) {
        return builder.routes()
                .route("geoserver_wms", r -> r
                        .path("/geoserver/wms")
                        .filters(f -> f.filters(gatewayFilters))
                        .uri(gatewayProperties.getGeoserverUri()))
                .build();
    }
}
