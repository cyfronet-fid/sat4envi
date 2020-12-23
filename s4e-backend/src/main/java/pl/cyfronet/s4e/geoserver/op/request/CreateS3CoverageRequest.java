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

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;

import java.util.List;


/**
 * <pre>
 *     {
 *         "coverage": {
 *             "namespace": "{workspace}",
 *             "store": "{workspace}:{coverageStore}",
 *             "name": "{coverage}",
 *             "title": "{coverage}",
 *             "srs": "{SRS}",
 *             "type": "{S3_COVERAGE_TYPE}",
 *             "nativeFormat": "{S3_STORE_TYPE}",
 *             "enabled": "true",
 *             "metadata": {
 *                  "entry":[
 *                      {
 *                          "@key" : "time",
 *                          "dimensionInfo" : {
 *                              "enabled" : true,
 *                              "presentation" : "CONTINUOUS_INTERVAL",
 *                              "units" : "ISO8601",
 *                              "defaultValue" : {
 *                                  "strategy" : "MINIMUM"
 *                              }
 *                          }
 *                      }
 *                  ]
 *              }
 *         }
 *     }
 * </pre>
 */
@Value
public class CreateS3CoverageRequest {
    @Value
    @Builder
    private static class Coverage {
        String namespace;
        String store;
        String name;
        String title;
        String srs = RequestConstants.SRS;
        String type = RequestConstants.S3_COVERAGE_TYPE;
        String nativeFormat = RequestConstants.S3_STORE_TYPE;
        boolean enabled = true;
        Metadata metadata;
    }

    @Value
    @Builder
    private static class Metadata {
        List<Entry> entry;
    }

    @Value
    @Builder
    private static class Entry {
        @JsonProperty("@key")
        String key = "time";
        DimensionInfo dimensionInfo;
    }

    @Value
    @Builder
    private static class DimensionInfo {
        boolean enabled = true;
        String presentation = "CONTINUOUS_INTERVAL";
        String units = "ISO8601";
        DefaultValue defaultValue;
    }

    @Value
    @Builder
    private static class DefaultValue {
        /**
         * It is fine to be MINIMUM, even though it could come to mind that MAXIMUM is a better strategy.
         * It could be because max means serving the most recent granule - however,
         * that would open up a possibility to get unlicensed EUMETSAT granules.
         */
        String strategy = "MINIMUM";
    }

    Coverage coverage;

    public CreateS3CoverageRequest(String workspace, String coverageStore, String coverage) {
        this.coverage = Coverage.builder()
                .namespace(workspace)
                .store(workspace + ":" + coverageStore)
                .name(coverage)
                .title(coverage)
                .metadata(Metadata.builder()
                        .entry(List.of(Entry.builder()
                                .dimensionInfo(DimensionInfo.builder()
                                        .defaultValue(DefaultValue.builder()
                                                .build())
                                        .build())
                                .build()))
                        .build())
                .build();
    }
}
