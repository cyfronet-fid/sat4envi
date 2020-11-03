package pl.cyfronet.s4e.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@ConfigurationProperties("mail")
@Validated
@Setter
@Getter
public class MailProperties {
    /**
     * The base url for use in mails, for example to fetch images.
     * <p>
     * Without the trailing slash, including the protocol.
      */
    @NotBlank
    @Pattern(regexp = "https?://[^/]+")
    private String urlDomain;
}
