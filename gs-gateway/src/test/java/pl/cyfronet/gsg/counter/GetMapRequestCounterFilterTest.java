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

package pl.cyfronet.gsg.counter;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class GetMapRequestCounterFilterTest {
    @Mock
    private GatewayFilterChain filterChain;

    @Mock
    private MetricService metricService;

    @Test
    public void shouldCountGetMap() {
        GlobalFilter filter = new GetMapRequestCounterFilter(metricService);
        MockServerHttpRequest request = MockServerHttpRequest.get("http://localhost")
                .queryParam("REQUEST", "GetMap")
                .queryParam("LAYERS", "msg")
                .build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);
        when(filterChain.filter(exchange)).thenReturn(Mono.empty());
        filter.filter(exchange, filterChain).block();

        assertThat(exchange.getResponse().isCommitted(), is(equalTo(false)));
        verify(filterChain).filter(exchange);
        verify(metricService).incrementCounter(anyString());
        verifyNoMoreInteractions(filterChain);
        verifyNoMoreInteractions(metricService);
    }

    @Test
    public void shouldntCountGetCapabilities() {
        GlobalFilter filter = new GetMapRequestCounterFilter(metricService);
        MockServerHttpRequest request = MockServerHttpRequest.get("http://localhost")
                .queryParam("REQUEST", "GetCapabilities")
                .build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);

        filter.filter(exchange, filterChain);

        assertThat(exchange.getResponse().isCommitted(), is(equalTo(false)));
        verify(filterChain).filter(exchange);
        verifyNoInteractions(metricService);
        verifyNoMoreInteractions(filterChain);
    }
}
