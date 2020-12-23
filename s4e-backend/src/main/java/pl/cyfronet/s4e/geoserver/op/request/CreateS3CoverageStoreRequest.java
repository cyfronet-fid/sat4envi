/*
 * Copyright 2020 ACC Cyfronet AGH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package pl.cyfronet.s4e.geoserver.op.request;

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
public class CreateS3CoverageStoreRequest {
    @Value
    @Builder
    private static class CoverageStore {
        String workspace;
        String name;
        String url;
        String type = RequestConstants.S3_STORE_TYPE;
        boolean enabled = true;
    }

    CoverageStore coverageStore;

    public CreateS3CoverageStoreRequest(String workspace, String coverageStore, String s3url) {
        this.coverageStore = CoverageStore.builder()
                .workspace(workspace)
                .name(coverageStore)
                .url(s3url)
                .build();
    }
}
