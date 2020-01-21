package pl.cyfronet.s4e.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;

@ConfigurationProperties("geoserver")
@Setter
@Getter
@Validated
public class GeoServerProperties {
    @NotBlank
    private String outsideBaseUrl;
    @NotBlank
    private String workspace;
    @NotBlank
    private String username;
    @NotBlank
    private String password;
    @NotBlank
    private String baseUrl;
    @NotBlank
    private String endpoint;
    private Long timeoutConnect = 60L;
    private Long timeoutRead = 60L;
}
