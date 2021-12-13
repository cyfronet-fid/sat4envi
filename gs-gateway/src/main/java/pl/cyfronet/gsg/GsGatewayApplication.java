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

package pl.cyfronet.gsg;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.jackson.io.JacksonDeserializer;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.sentry.Sentry;
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
import pl.cyfronet.gsg.counter.GetMapRequestCounterFilter;
import pl.cyfronet.gsg.counter.MetricService;
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
        Sentry.init(options -> {
            options.setDsn("");
            options.setTracesSampleRate(0.5);
            options.setEnableExternalConfiguration(true);
        });

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
    public Counter mapRequestCounter(MeterRegistry registry) {
        return Counter.builder("mapRequestCounter").register(registry);
    }

    @Bean
    public Counter sentinel1RequestCounter(MeterRegistry registry) {
        return Counter.builder("sentinel1RequestCounter").register(registry);
    }

    @Bean
    public Counter sentinel2RequestCounter(MeterRegistry registry) {
        return Counter.builder("sentinel2RequestCounter").register(registry);
    }

    @Bean
    public Counter sentinel3RequestCounter(MeterRegistry registry) {
        return Counter.builder("sentinel3RequestCounter").register(registry);
    }

    @Bean
    public Counter sentinel5PRequestCounter(MeterRegistry registry) {
        return Counter.builder("sentinel5PRequestCounter").register(registry);
    }

    @Bean
    public MetricService metricService(Counter mapRequestCounter,
                                       Counter sentinel1RequestCounter,
                                       Counter sentinel2RequestCounter,
                                       Counter sentinel3RequestCounter,
                                       Counter sentinel5PRequestCounter) {
        return new MetricService(mapRequestCounter,
                sentinel1RequestCounter,
                sentinel2RequestCounter,
                sentinel3RequestCounter,
                sentinel5PRequestCounter);
    }

    @Bean
    public GetMapRequestCounterFilter getMapRequestCounterFilter(MetricService metricService) {
        return new GetMapRequestCounterFilter(metricService);
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
