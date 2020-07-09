package pl.cyfronet.gsg.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.jackson.io.JacksonDeserializer;
import io.jsonwebtoken.jackson.io.JacksonSerializer;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;

import java.security.Key;
import java.security.KeyPair;
import java.util.Set;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
class JwtGlobalFilterTest {
    private static final KeyPair DEV_KEY_PAIR;
    private static final ObjectMapper OBJECT_MAPPER;
    private static final JwtParser JWT_PARSER;
    private static final Object SKIP_LAYERS_CLAIM = "SKIP_LAYERS_CLAIM";

    static {
        try {
            OBJECT_MAPPER = new ObjectMapper();
            DEV_KEY_PAIR = LoadKeyPairTestHelper.loadKeyPair();
            JWT_PARSER = Jwts.parserBuilder()
                    .setSigningKey(DEV_KEY_PAIR.getPublic())
                    .deserializeJsonWith(new JacksonDeserializer<>(OBJECT_MAPPER))
                    .build();
        } catch (Exception e) {
            // Fail fast if the key doesn't load.
            throw new RuntimeException(e);
        }
    }

    @Mock
    private GatewayFilterChain filterChain;

    @Test
    public void shouldPassRequestWithoutAuthorizationHeader() {
        GlobalFilter filter = new JwtGlobalFilter(JWT_PARSER);
        MockServerHttpRequest request = MockServerHttpRequest.get("http://localhost").build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);

        filter.filter(exchange, filterChain);

        assertThat(exchange.getAttributes().containsKey(JwtGlobalFilter.LICENSED_LAYERS_ATTR), is(false));
        verify(filterChain).filter(exchange);
        verifyNoMoreInteractions(filterChain);
    }

    @ParameterizedTest
    @MethodSource
    public void shouldParseLayersFromAuthorizationHeader(Object layersClaim, Set<String> expectedLayers) {
        GlobalFilter filter = new JwtGlobalFilter(JWT_PARSER);
        MockServerHttpRequest request = MockServerHttpRequest.get("http://localhost")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt(layersClaim))
                .build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);

        filter.filter(exchange, filterChain);

        assertThat(exchange.getAttribute(JwtGlobalFilter.LICENSED_LAYERS_ATTR), is(equalTo(expectedLayers)));
        verify(filterChain).filter(exchange);
        verifyNoMoreInteractions(filterChain);
    }

    public static Stream<Arguments> shouldParseLayersFromAuthorizationHeader() {
        return Stream.of(
                Arguments.of(null, Set.of()),
                Arguments.of(Set.of(), Set.of()),
                Arguments.of(Set.of("layer_1"), Set.of("layer_1")),
                Arguments.of(Set.of("layer_1", "layer_2"), Set.of("layer_1", "layer_2")),
                Arguments.of("Some other type than Collection", Set.of()),
                Arguments.of(SKIP_LAYERS_CLAIM, Set.of())
        );
    }

    @ParameterizedTest
    @MethodSource
    public void shouldParseLayersFromAuthorizationCookie(Object layersClaim, Set<String> expectedLayers) {
        GlobalFilter filter = new JwtGlobalFilter(JWT_PARSER);
        MockServerHttpRequest request = MockServerHttpRequest.get("http://localhost")
                .cookie(new HttpCookie("token", jwt(layersClaim)))
                .build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);

        filter.filter(exchange, filterChain);

        assertThat(exchange.getAttribute(JwtGlobalFilter.LICENSED_LAYERS_ATTR), is(equalTo(expectedLayers)));
        verify(filterChain).filter(exchange);
        verifyNoMoreInteractions(filterChain);
    }

    public static Stream<Arguments> shouldParseLayersFromAuthorizationCookie() {
        return Stream.of(
                Arguments.of(null, Set.of()),
                Arguments.of(Set.of(), Set.of()),
                Arguments.of(Set.of("layer_1"), Set.of("layer_1")),
                Arguments.of(Set.of("layer_1", "layer_2"), Set.of("layer_1", "layer_2")),
                Arguments.of("Some other type than Collection", Set.of()),
                Arguments.of(SKIP_LAYERS_CLAIM, Set.of())
        );
    }

    @Test
    public void betterSafeThanSorry() {
        GlobalFilter filter = new JwtGlobalFilter(JWT_PARSER);

        // This request will come with a JWT signed with the public key. If this worked, it could be exploited in such a way:
        // https://www.pingidentity.com/en/company/blog/posts/2019/jwt-security-nobody-talks-about.html
        // JJWT handles this case, but this test makes sure we didn't make some configuration mistake.
        Key key = Keys.hmacShaKeyFor(DEV_KEY_PAIR.getPublic().getEncoded());
        MockServerHttpRequest request = MockServerHttpRequest.get("http://localhost")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt(Set.of("layer_1"), key))
                .build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);

        filter.filter(exchange, filterChain);

        assertThat(exchange.getAttributes().containsKey(JwtGlobalFilter.LICENSED_LAYERS_ATTR), is(false));
        verify(filterChain).filter(exchange);
        verifyNoMoreInteractions(filterChain);
    }

    private static String jwt(Object layers) {
        return jwt(layers, DEV_KEY_PAIR.getPrivate());
    }

    private static String jwt(Object layers, Key key) {
        JwtBuilder builder = Jwts.builder()
                .setSubject("test");
        if (layers != SKIP_LAYERS_CLAIM) {
            builder.claim("layers", layers);
        }
        return builder
                .signWith(key)
                .serializeToJsonWith(new JacksonSerializer<>(OBJECT_MAPPER))
                .compact();
    }
}
