package pl.cyfronet.s4e.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;

@ConfigurationProperties("expert.help")
@Validated
@Setter
@Getter
public class ExpertHelpProperties {
    /**
     * Email to expert
     */
    @NotNull
    @Email
    private String mail;
}
