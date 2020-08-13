package pl.cyfronet.s4e.admin.sync.api;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class ErrorsResponse {
    private long successesCount;
    private long runningCount;
    private long errorsCount;
    private List<ErrorResponse> errors;

    @Data
    public static class ErrorResponse {
        private String sceneKey;
        private String code;
        private Exception cause;
        private Map<String, Object> parameters;
    }
}
