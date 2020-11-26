package pl.cyfronet.s4e.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotNull;
import java.util.Set;

@ConfigurationProperties("scene.artifacts")
@Validated
@Setter
@Getter
public class SceneArtifactsProperties {
    @NotNull
    private Set<String> internalDownloadWhitelist;
}
