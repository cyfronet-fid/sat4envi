package pl.cyfronet.s4e.sync;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Slf4j
public class LoggingAcceptor implements SceneAcceptor {
    private final SceneAcceptor delegate;

    @Override
    public Error accept(String sceneKey) {
        Error error = delegate.accept(sceneKey);
        if (error != null) {
            handleError(error);
        }
        return error;
    }

    private void handleError(Error error) {
        StringBuilder sb = createBuilderAndAppendSceneKey(error.getSceneKey());
        appendCode(sb, error.getCode());
        appendParameters(sb, error.getParameters());
        log.warn(sb.toString(), error.getCause());
    }

    private StringBuilder createBuilderAndAppendSceneKey(String key) {
        StringBuilder sb = new StringBuilder();
        sb.append("Error processing key: '").append(key).append("'.");
        return sb;
    }

    private void appendCode(StringBuilder sb, String code) {
        if (code != null) {
            sb.append(" Error code: ").append(code).append(".");
        }
    }

    private void appendParameters(StringBuilder sb, Map<String, Object> parameters) {
        if (parameters != null && !parameters.isEmpty()) {
            List<String> mappedParameters = parameters.entrySet().stream()
                    .map(e -> e.getKey() + "=>" + e.getValue())
                    .collect(Collectors.toList());
            sb.append(" Parameters: ").append(String.join(", ", mappedParameters)).append(".");
        }
    }
}
