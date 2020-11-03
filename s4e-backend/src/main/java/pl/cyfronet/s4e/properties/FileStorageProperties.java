package pl.cyfronet.s4e.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

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
     * A path prefix for generated links, including the leading and trailing slash.
     */
    @NotBlank
    @Pattern(regexp = "/.+/")
    private String pathPrefix = "/static/";

    /**
     * A key prefix for thumbnails, including the trailing slash.
     */
    @NotBlank
    @Pattern(regexp = "[^/].*/")
    private String keyPrefixThumbnail;

    /**
     * A key prefix for institutions emblem, including the trailing slash.
     */
    @NotBlank
    @Pattern(regexp = "[^/].*/")
    private String keyPrefixEmblem;

    /**
     * A key prefix for products categories icons, including the trailing slash.
     */
    @NotBlank
    @Pattern(regexp = "[^/].*/")
    private String keyPrefixProductsCategoriesIcons;
}
