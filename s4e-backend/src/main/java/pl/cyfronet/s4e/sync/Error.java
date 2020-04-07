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

    private final String sceneKey;
    private final String code;
    private final Exception cause;
    @Singular
    private final Map<String, Object> parameters;

    public static ErrorBuilder builder(String sceneKey) {
        return new ErrorBuilder().sceneKey(sceneKey);
    }
}
