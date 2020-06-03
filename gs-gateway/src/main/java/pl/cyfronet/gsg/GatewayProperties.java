package pl.cyfronet.gsg;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotEmpty;
import java.time.Duration;
import java.util.HashSet;
import java.util.Set;

@ConfigurationProperties(prefix = "gateway")
@Validated
@Getter
@Setter
public class GatewayProperties {
    @NotEmpty
    private String geoserverUri;

    private Set<String> allowedParams = new HashSet<>();

    private Set<String> openLayers = new HashSet<>();

    private Set<String> eumetsatLayers = new HashSet<>();

    private QueryParams queryParams = new QueryParams();

    private Duration freshDuration = Duration.ofHours(3);

    @Validated
    @Getter
    @Setter
    public static class QueryParams {
        private String layers = "LAYERS";
        private String time = "TIME";
    }
}
