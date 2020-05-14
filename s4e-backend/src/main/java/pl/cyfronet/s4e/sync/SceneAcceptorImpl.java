package pl.cyfronet.s4e.sync;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pl.cyfronet.s4e.sync.context.Context;
import pl.cyfronet.s4e.sync.step.Step;

import java.util.List;

@RequiredArgsConstructor
@Slf4j
public class SceneAcceptorImpl implements SceneAcceptor {
    private final PipelineFactory pipelineFactory;

    @Override
    public Error accept(String sceneKey) {
        Error result = null;
        Context context = new Context(sceneKey);
        try {
            List<Step<Context, Error>> pipeline = pipelineFactory.build();
            for (Step<Context, Error> step : pipeline) {
                result = step.apply(context);
                if (result != null) {
                    break;
                }
            }
        } catch (RuntimeException e) {
            result = context.getError().cause(e).build();
        }
        return result;
    }
}
