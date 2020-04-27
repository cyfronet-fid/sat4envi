package pl.cyfronet.s4e.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotEmpty;

@ConfigurationProperties("jwt")
@Validated
@Setter
@Getter
public class JwtProperties {
    @NotEmpty
    private String keyStore;

    @NotEmpty
    private String keyStorePassword;

    @NotEmpty
    private String keyAlias;

    @NotEmpty
    private String keyPassword;
}
