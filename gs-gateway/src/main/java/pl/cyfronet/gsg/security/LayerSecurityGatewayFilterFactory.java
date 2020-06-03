package pl.cyfronet.gsg.security;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;

import java.time.Clock;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class LayerSecurityGatewayFilterFactory extends AbstractGatewayFilterFactory<LayerSecurityGatewayFilterFactory.Config> {
    private final Clock clock;

    public LayerSecurityGatewayFilterFactory(Clock clock) {
        super(Config.class);
        this.clock = clock;
    }

    @Override
    public GatewayFilter apply(Config config) {
        val layersQueryParam = config.getLayersQueryParam();
        val timeQueryParam = config.getTimeQueryParam();
        val freshDuration = config.getFreshDuration();
        val access = config.getAccess();

        return new LayerSecurityGatewayFilter(clock, layersQueryParam, timeQueryParam, freshDuration, access);
    }

    @Setter
    @Getter
    public static class Config {
        private String layersQueryParam = "LAYERS";
        private String timeQueryParam = "TIME";

        private Duration freshDuration = Duration.ofHours(3);

        private Map<String, AccessType> access = new HashMap<>();
    }
}
