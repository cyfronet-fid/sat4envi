package pl.cyfronet.s4e.service.payload;

import lombok.Builder;
import lombok.Value;


/**
 * <pre>
 *     {
 *         "coverageStore": {
 *             "workspace": "{workspace}",
 *             "name": "{coverageStore}",
 *             "url": "{s3url}",
 *             "type": "{S3_STORE_TYPE}",
 *             "enabled": "true"
 *         }
 *     }
 * </pre>
 */
@Value
public class CreateS3CoverageStorePayload {
    @Value
    @Builder
    private static class CoverageStore {
        String workspace;
        String name;
        String url;
        String type = PayloadConstants.S3_STORE_TYPE;
        boolean enabled = true;
    }

    CoverageStore coverageStore;

    public CreateS3CoverageStorePayload(String workspace, String coverageStore, String s3url) {
        this.coverageStore = CoverageStore.builder()
                .workspace(workspace)
                .name(coverageStore)
                .url(s3url)
                .build();
    }


}
