package pl.cyfronet.s4e.sync.step;

import lombok.Builder;
import lombok.val;
import pl.cyfronet.s4e.sync.Error;
import pl.cyfronet.s4e.sync.context.BaseContext;

import java.util.Map;
import java.util.function.Function;

@Builder
public class ResetErrorParameters<T extends BaseContext> implements Step<T, Error> {
    private final Function<T, Map<String, String>> parameters;

    @Override
    public Error apply(T context) {
        val error = context.getError();
        error.clearParameters();
        error.parameters(parameters.apply(context));
        return null;
    }
}
