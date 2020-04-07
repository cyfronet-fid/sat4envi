package pl.cyfronet.s4e.sync.step;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.cyfronet.s4e.sync.Error;
import pl.cyfronet.s4e.sync.context.BaseContext;

import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class ResetErrorParametersTest extends BaseStepTest<BaseContext> {

    private ResetErrorParameters step;

    private Map<String, String> parameters;

    @BeforeEach
    public void beforeEach() {
        stubContext();

        step = ResetErrorParameters.builder()
                .parameters(c -> parameters)
                .build();
    }

    @Test
    public void shouldWork() {
        parameters = Map.of("param", "value");

        Error error = step.apply(context);

        assertThat(error, is(nullValue()));
        Error builtError = context.getError().build();
        assertThat(builtError.getParameters(), is(equalTo(parameters)));
    }

    @Test
    public void shouldResetParameters() {
        parameters = Map.of("param", "value");
        context.getError().parameters(Map.of("other", "value"));

        Error error = step.apply(context);

        assertThat(error, is(nullValue()));
        Error builtError = context.getError().build();
        assertThat(builtError.getParameters(), is(equalTo(parameters)));
    }
}
