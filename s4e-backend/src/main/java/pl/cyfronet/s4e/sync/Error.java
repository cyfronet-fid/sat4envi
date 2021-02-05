/*
 * Copyright 2021 ACC Cyfronet AGH
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

package pl.cyfronet.s4e.sync;

import lombok.Builder;
import lombok.Getter;
import lombok.Singular;

import java.util.Map;

@Builder
@Getter
public class Error {
    public static final String ERR_S3_CLIENT_EXCEPTION = "s3_client_exception";

    public static final String ERR_FILE_NOT_FOUND = "file_not_found";
    public static final String ERR_INVALID_JSON = "invalid_json";
    public static final String ERR_NO_SCHEMA_PROPERTY = "no_schema_property";
    public static final String ERR_VIOLATES_SCHEMA = "violates_schema";
    public static final String ERR_SCHEMA_NOT_FOUND = "schema_not_found";
    public static final String ERR_SCHEMA_WRONG_TYPE = "schema_wrong_type";
    public static final String ERR_SCHEMA_PRODUCT_MISMATCH = "schema_product_mismatch";
    public static final String ERR_PRODUCT_NOT_FOUND = "product_not_found";
    public static final String ERR_ARTIFACTS_NOT_FOUND = "artifacts_not_found";
    public static final String ERR_ARTIFACT_PATH_INCORRECT = "artifact_path_incorrect";
    public static final String ERR_METADATA_FOOTPRINT_TRANSFORM_FAILED = "metadata_footprint_transform_failed";
    public static final String ERR_SCENE_S3PATH_NULL = "scene_s3path_null";

    private final String sceneKey;
    private final String code;
    private final Exception cause;
    @Singular
    private final Map<String, Object> parameters;

    public static ErrorBuilder builder(String sceneKey) {
        return new ErrorBuilder().sceneKey(sceneKey);
    }
}
