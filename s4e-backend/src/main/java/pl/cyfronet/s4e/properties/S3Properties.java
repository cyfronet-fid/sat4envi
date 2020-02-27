package pl.cyfronet.s4e.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.net.URI;
import java.time.Duration;

@ConfigurationProperties("s3")
@Validated
@Setter
@Getter
public class S3Properties {
    @NotBlank
    private String accessKey;
    @NotBlank
    private String secretKey;
    @NotNull
    private URI endpoint;
    /**
     * A bucket name to use.
     *
     * <p>
     * This is the bucket which contains scenes.
     */
    @NotBlank
    private String bucket;
    /**
     * The validity duration of a generated download link.
     */
    @NotNull
    private Duration presignedGetTimeout;
}
