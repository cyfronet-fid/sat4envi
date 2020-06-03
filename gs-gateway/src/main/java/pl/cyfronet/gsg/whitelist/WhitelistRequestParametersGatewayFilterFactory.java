package pl.cyfronet.gsg.whitelist;

import lombok.Getter;
import lombok.Setter;
import lombok.val;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;

import java.util.HashSet;
import java.util.Set;

public class WhitelistRequestParametersGatewayFilterFactory extends AbstractGatewayFilterFactory<WhitelistRequestParametersGatewayFilterFactory.Config> {
    public WhitelistRequestParametersGatewayFilterFactory() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        val allowedParams = config.getAllowedParams();
        return new WhitelistRequestParametersGatewayFilter(allowedParams);
    }

    @Setter
    @Getter
    public static class Config {
        private Set<String> allowedParams = new HashSet<>();
    }
}
