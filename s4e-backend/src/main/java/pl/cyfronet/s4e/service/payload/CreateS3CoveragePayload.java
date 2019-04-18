package pl.cyfronet.s4e.service.payload;

import lombok.Builder;
import lombok.Value;


/**
 * <pre>
 *     {
 *         "coverage": {
 *             "namespace": "{workspace}",
 *             "store": "{workspace}:{coverageStore}",
 *             "name": "{coverage}",
 *             "title": "{coverage}",
 *             "srs": "{SRS}",
 *             "nativeFormat": "{S3_STORE_TYPE}",
 *             "enabled": "true"
 *         }
 *     }
 * </pre>
 */
@Value
public class CreateS3CoveragePayload {
    @Value
    @Builder
    private static class Coverage {
        String namespace;
        String store;
        String name;
        String title;
        String srs = PayloadConstants.SRS;
        String nativeFormat = PayloadConstants.S3_STORE_TYPE;
        boolean enabled = true;
    }

    Coverage coverage;

    public CreateS3CoveragePayload(String workspace, String coverageStore, String coverage) {
        this.coverage = Coverage.builder()
                .namespace(workspace)
                .store(workspace+":"+coverageStore)
                .name(coverage)
                .title(coverage)
                .build();
    }
}
