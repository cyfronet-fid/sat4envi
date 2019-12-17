package pl.cyfronet.s4e;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.validation.constraints.NotBlank;
import java.net.URI;

@ConfigurationProperties("s3.client")
@Setter
@Getter
public class S3ClientProperties {
    @NotBlank
    private String accessKey;
    @NotBlank
    private String secretKey;
    @NotBlank
    private URI endpoint;
}
