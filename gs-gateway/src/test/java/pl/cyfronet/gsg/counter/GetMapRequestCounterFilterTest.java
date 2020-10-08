package pl.cyfronet.gsg.counter;

import io.micrometer.core.instrument.Counter;
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
    private Counter mapRequestCounter;

    @Test
    public void shouldCountGetMap() {
        GlobalFilter filter = new GetMapRequestCounterFilter(mapRequestCounter);
        MockServerHttpRequest request = MockServerHttpRequest.get("http://localhost")
                .queryParam("REQUEST", "GetMap")
                .build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);
        when(filterChain.filter(exchange)).thenReturn(Mono.empty());
        filter.filter(exchange, filterChain).block();

        assertThat(exchange.getResponse().isCommitted(), is(equalTo(false)));
        verify(filterChain).filter(exchange);
        verify(mapRequestCounter).increment();
        verifyNoMoreInteractions(filterChain);
        verifyNoMoreInteractions(mapRequestCounter);
    }

    @Test
    public void shouldntCountGetCapabilities() {
        GlobalFilter filter = new GetMapRequestCounterFilter(mapRequestCounter);
        MockServerHttpRequest request = MockServerHttpRequest.get("http://localhost")
                .queryParam("REQUEST", "GetCapabilities")
                .build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);

        filter.filter(exchange, filterChain);

        assertThat(exchange.getResponse().isCommitted(), is(equalTo(false)));
        verify(filterChain).filter(exchange);
        verifyNoInteractions(mapRequestCounter);
        verifyNoMoreInteractions(filterChain);
    }
}
