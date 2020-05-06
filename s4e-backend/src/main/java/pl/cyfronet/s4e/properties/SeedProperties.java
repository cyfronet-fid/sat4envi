package pl.cyfronet.s4e.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
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
    @NestedConfigurationProperty
    private S4eSyncV1 s4eSyncV1 = new S4eSyncV1();

    @Getter
    @Setter
    public static class S4eSyncV1 {
        /// Set to -1 to disable the limit.
        private int limit = 100;
    }
}
