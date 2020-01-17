package pl.cyfronet.s4e;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;

@ConfigurationProperties("mail")
@Validated
@Setter
@Getter
public class MailProperties {
    /**
     * The base url for use in mails, for example to fetch images.
     * <p>
     * Without the trailing slash.
      */
    @NotBlank
    private String urlDomain;
}
