package pl.cyfronet.s4e.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotEmpty;

@ConfigurationProperties("amqp")
@Validated
@Setter
@Getter
public class AmqpProperties {
    private boolean enabled = false;

    private boolean createQueues = false;

    @NestedConfigurationProperty
    private Queues queues = new Queues();

    @Getter
    @Setter
    public static class Queues {
        @NotEmpty
        private String incoming = "s4e.scenes.incoming";
    }
}
