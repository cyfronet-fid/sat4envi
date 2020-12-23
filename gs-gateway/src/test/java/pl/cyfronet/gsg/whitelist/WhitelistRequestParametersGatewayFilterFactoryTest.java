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

package pl.cyfronet.gsg.whitelist;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WhitelistRequestParametersGatewayFilterFactoryTest {
    @Mock
    private GatewayFilterChain filterChain;

    @Captor
    private ArgumentCaptor<ServerWebExchange> captor;

    @Test
    public void shouldFilterQueryParams() {
        MockServerHttpRequest request = MockServerHttpRequest.get("http://localhost")
                .queryParam("allowed", "42")
                .queryParam("not-allowed", "24").build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);
        WhitelistRequestParametersGatewayFilterFactory.Config config = new WhitelistRequestParametersGatewayFilterFactory.Config();
        config.setAllowedParams(Set.of("allowed"));
        GatewayFilter filter = new WhitelistRequestParametersGatewayFilterFactory().apply(config);
        when(filterChain.filter(captor.capture())).thenReturn(Mono.empty());

        filter.filter(exchange, filterChain);

        ServerHttpRequest actualRequest = captor.getValue().getRequest();
        assertThat(actualRequest.getQueryParams().keySet(), containsInAnyOrder("allowed"));
    }
}
