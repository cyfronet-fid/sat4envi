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

package pl.cyfronet.gsg.security;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import pl.cyfronet.gsg.jwt.JwtGlobalFilter;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LayerSecurityGatewayFilterTest {
    @Mock
    private GatewayFilterChain filterChain;

    @Test
    public void shouldAllowOpenLayer() {
        Clock clock = Clock.systemUTC();
        GatewayFilter filter = new LayerSecurityGatewayFilter(
                clock, "LAYERS", null, null, Map.of("layer_1", AccessType.OPEN));
        MockServerHttpRequest request = MockServerHttpRequest.get("http://localhost")
                .queryParam("LAYERS", "layer_1")
                .build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);

        filter.filter(exchange, filterChain);

        assertThat(exchange.getResponse().isCommitted(), is(equalTo(false)));
        verify(filterChain).filter(exchange);
        verifyNoMoreInteractions(filterChain);
    }

    @Test
    public void shouldAllowGetCapabilitiesRequests() {
        Clock clock = Clock.systemUTC();
        GatewayFilter filter = new LayerSecurityGatewayFilter(
                clock, "LAYERS", null, null, Map.of("layer_1", AccessType.PRIVATE));
        MockServerHttpRequest request = MockServerHttpRequest.get("http://localhost")
                .queryParam("LAYERS", "layer_1")
                .queryParam("REQUEST", "GetCapabilities")
                .build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);

        filter.filter(exchange, filterChain);

        assertThat(exchange.getResponse().isCommitted(), is(equalTo(false)));
        verify(filterChain).filter(exchange);
        verifyNoMoreInteractions(filterChain);
    }

    @Test
    public void shouldRejectByDefault() {
        Clock clock = Clock.systemUTC();
        GatewayFilter filter = new LayerSecurityGatewayFilter(
                clock, "LAYERS", null, null, Map.of());
        MockServerHttpRequest request = MockServerHttpRequest.get("http://localhost")
                .queryParam("LAYERS", "layer_1")
                .build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);

        filter.filter(exchange, filterChain);

        assertThat(exchange.getResponse().isCommitted(), is(equalTo(true)));
        assertThat(exchange.getResponse().getStatusCode(), is(equalTo(HttpStatus.UNAUTHORIZED)));
        verifyNoInteractions(filterChain);
    }

    @ParameterizedTest
    @MethodSource
    public void shouldHandleEumetsatLicense(String time, Set<String> licensedLayers, boolean expectedIsCommited, HttpStatus expectedStatusCode) {
        Instant baseTime = Instant.parse("2007-12-03T10:15:30.00Z");
        Clock clock = Clock.fixed(baseTime, ZoneId.systemDefault());
        GatewayFilter filter = new LayerSecurityGatewayFilter(
                clock, "LAYERS", "TIME", Duration.ofHours(3), Map.of("layer_1", AccessType.EUMETSAT));
        MockServerHttpRequest request = MockServerHttpRequest.get("http://localhost")
                .queryParam("LAYERS", "layer_1")
                .queryParam("TIME", time)
                .build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);
        if (licensedLayers != null) {
            exchange.getAttributes().put(JwtGlobalFilter.LICENSED_LAYERS_ATTR, licensedLayers);
        }

        filter.filter(exchange, filterChain);

        assertThat(exchange.getResponse().isCommitted(), is(equalTo(expectedIsCommited)));
        assertThat(exchange.getResponse().getStatusCode(), is(equalTo(expectedStatusCode)));
        if (expectedIsCommited) {
            verifyNoInteractions(filterChain);
        } else {
            verify(filterChain).filter(exchange);
            verifyNoMoreInteractions(filterChain);
        }
    }

    private static Stream<Arguments> shouldHandleEumetsatLicense() {
        return Stream.of(
                Arguments.of("2007-12-03T10:01:00.00Z", null,              true,  HttpStatus.UNAUTHORIZED),
                Arguments.of("2007-12-03T10:01:00.00Z", Set.of(),          true,  HttpStatus.FORBIDDEN),
                Arguments.of("2007-12-03T10:00:59.99Z", null,              false, null),
                Arguments.of("2007-12-03T10:00:59.99Z", Set.of(),          false, null),
                Arguments.of("2007-12-03T10:00:00.00Z", null,              false, null),
                Arguments.of("2007-12-03T10:00:00.00Z", Set.of(),          false, null),
                Arguments.of("2007-12-03T09:59:59.99Z", null,              true,  HttpStatus.UNAUTHORIZED),
                Arguments.of("2007-12-03T09:59:59.99Z", Set.of(),          true,  HttpStatus.FORBIDDEN),
                Arguments.of("2007-12-03T09:00:00.00Z", null,              false, null),
                Arguments.of("2007-12-03T09:00:00.00Z", Set.of(),          false, null),
                Arguments.of("2007-12-03T08:00:00.00Z", null,              false, null),
                Arguments.of("2007-12-03T08:00:00.00Z", Set.of(),          false, null),
                Arguments.of("2007-12-03T07:15:31.00Z", null,              true,  HttpStatus.UNAUTHORIZED),
                Arguments.of("2007-12-03T07:15:31.00Z", Set.of(),          true,  HttpStatus.FORBIDDEN),
                Arguments.of("2007-12-03T07:15:31.00Z", Set.of("layer_1"), false, null),
                Arguments.of("2007-12-03T07:15:30.00Z", null,              false, null),
                Arguments.of("2007-12-03T07:15:30.00Z", Set.of(),          false, null),
                Arguments.of("2007-12-03T07:15:30.00Z", Set.of("layer_1"), false, null),
                Arguments.of("2007-12-03T07:15:29.00Z", null,              false, null)
        );
    }

    @ParameterizedTest
    @MethodSource
    public void shouldHandlePrivateLicense(Set<String> licensedLayers, boolean expectedIsCommited, HttpStatus expectedStatusCode) {
        // Every layer not listed in access is private by default
        Clock clock = Clock.systemUTC();
        GatewayFilter filter = new LayerSecurityGatewayFilter(
                clock, "LAYERS", null, null, Map.of());
        MockServerHttpRequest request = MockServerHttpRequest.get("http://localhost")
                .queryParam("LAYERS", "layer_1")
                .build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);
        if (licensedLayers != null) {
            exchange.getAttributes().put(JwtGlobalFilter.LICENSED_LAYERS_ATTR, licensedLayers);
        }

        filter.filter(exchange, filterChain);

        assertThat(exchange.getResponse().isCommitted(), is(equalTo(expectedIsCommited)));
        assertThat(exchange.getResponse().getStatusCode(), is(equalTo(expectedStatusCode)));
        if (expectedIsCommited) {
            verifyNoInteractions(filterChain);
        } else {
            verify(filterChain).filter(exchange);
            verifyNoMoreInteractions(filterChain);
        }
    }

    private static Stream<Arguments> shouldHandlePrivateLicense() {
        return Stream.of(
                Arguments.of(null,              true,  HttpStatus.UNAUTHORIZED),
                Arguments.of(Set.of(),          true,  HttpStatus.FORBIDDEN),
                Arguments.of(Set.of("layer_1"), false, null)
        );
    }
}
