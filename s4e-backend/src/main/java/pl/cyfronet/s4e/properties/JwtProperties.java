package pl.cyfronet.s4e.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import java.time.Duration;

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

    @NestedConfigurationProperty
    private Token token = new Token();

    @NestedConfigurationProperty
    private Cookie cookie = new Cookie();

    @Getter
    @Setter
    public static class Token {
        /// Half a day. In case of an 8h shift require a login every day.
        private Duration expirationTime = Duration.ofHours(12);
    }

    @Getter
    @Setter
    public static class Cookie {
        @Pattern(regexp = "[^/:]+")
        private String domain;
    }
}
