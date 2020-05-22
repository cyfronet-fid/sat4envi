package pl.cyfronet.s4e.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.validation.constraints.NotBlank;

@ConfigurationProperties("s3.file-storage")
@Setter
@Getter
public class FileStorageProperties {
    /**
     * A bucket name to use.
      */
    @NotBlank
    private String bucket;

    /**
     * A key prefix for thumbnails, including the trailing slash.
     */
    @NotBlank
    private String keyPrefixThumbnail;

    /**
     * A key prefix for institutions emblem, including the trailing slash.
     */
    @NotBlank
    private String keyPrefixEmblem;
}
