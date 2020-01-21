package pl.cyfronet.s4e.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties("seed.products")
@Setter
@Getter
@Validated
public class SeedProperties {
    private boolean seedDb = true;
    private boolean syncGeoserver = true;
    private boolean syncGeoserverResetWorkspace = true;
    private String dataSet = "minio-data-v1";
}
