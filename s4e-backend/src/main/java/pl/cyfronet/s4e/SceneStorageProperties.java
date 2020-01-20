package pl.cyfronet.s4e;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;
import java.time.Duration;

@ConfigurationProperties("s3.scene-storage")
@Validated
@Setter
@Getter
public class SceneStorageProperties {
    /**
     * A bucket name to use.
      */
    @NotBlank
    private String bucket;

    /**
     * The validity duration of a generated download link.
     */
    private Duration presignedGetTimeout;
}
