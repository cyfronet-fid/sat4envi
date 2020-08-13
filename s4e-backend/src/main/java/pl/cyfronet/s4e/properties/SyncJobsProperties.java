package pl.cyfronet.s4e.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.Min;

@ConfigurationProperties("sync-jobs")
@Validated
@Setter
@Getter
public class SyncJobsProperties {
    /**
     * Keep in mind that the AWS SDK has a default connection pool size of 50, you can start
     * receiving errors if you attempt to open more connections in parallel.
     */
    @Min(1)
    private int threadPoolSize = 40;
}
