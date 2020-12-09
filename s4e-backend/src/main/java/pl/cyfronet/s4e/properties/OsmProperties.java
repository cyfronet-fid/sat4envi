package pl.cyfronet.s4e.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@ConfigurationProperties("osm")
@Setter
@Getter
@Validated
public class OsmProperties {
    /// Url from which OSM tiles will be fetched by front-end
    @NotBlank
    @Pattern(regexp = ".*/\\{z}/\\{x}/\\{y}.*")
    private String url;
}
