package pl.cyfronet.s4e.sync;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pl.cyfronet.s4e.sync.context.Context;
import pl.cyfronet.s4e.sync.step.Step;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class SceneAcceptor {
    private final PipelineFactory pipelineFactory;

    public void accept(String sceneKey) {
        Context context = new Context(sceneKey);
        try {
            List<Step<Context, Error>> pipeline = pipelineFactory.build();
            for (Step<Context, Error> step : pipeline) {
                Error error = step.apply(context);
                if (error != null) {
                    handleError(error);
                    break;
                }
            }
        } catch (RuntimeException e) {
            handleError(context.getError().cause(e).build());
        }
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

    private void handleError(Error error) {
        StringBuilder sb = createBuilderAndAppendSceneKey(error.getSceneKey());
        appendCode(sb, error.getCode());
        appendParameters(sb, error.getParameters());
        log.warn(sb.toString(), error.getCause());
    }
}
