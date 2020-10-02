package pl.cyfronet.s4e.sync.step;

import jakarta.json.JsonObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.leadpony.justify.api.JsonSchema;
import org.leadpony.justify.api.JsonValidatingException;
import org.leadpony.justify.api.JsonValidationService;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import pl.cyfronet.s4e.sync.Error;
import pl.cyfronet.s4e.sync.context.BaseContext;

import java.util.function.BiConsumer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static pl.cyfronet.s4e.sync.Error.ERR_VIOLATES_SCHEMA;

class LoadValidatedJsonTest extends BaseStepTest<BaseContext> {
    @Mock
    private BiConsumer<BaseContext, JsonObject> update;

    private String content;

    private JsonSchema jsonSchema;

    private LoadValidatedJson step;

    @BeforeEach
    public void beforeEach() {
        stubContext();

        content = "{\"schema\":\"my_schema\"}";
        jsonSchema = JsonSchema.TRUE;
        step = LoadValidatedJson.builder()
                .jsonValidationService(() -> JsonValidationService.newInstance())
                .content(c -> content)
                .jsonSchema(c -> jsonSchema)
                .update(update)
                .build();
    }

    @Test
    public void shouldWork() {
        Error error = step.apply(context);

        assertThat(error, is(nullValue()));
        verify(update).accept(eq(context), ArgumentMatchers.any(JsonObject.class));
        verifyNoMoreInteractions(update);
    }

    @Test
    public void shouldHandleValidationException() {
        jsonSchema = JsonSchema.FALSE;

        Error error = step.apply(context);

        assertThat(error, is(notNullValue()));
        assertThat(error.getCode(), is(equalTo(ERR_VIOLATES_SCHEMA)));
        assertThat(error.getCause(), is(instanceOf(JsonValidatingException.class)));
        verifyNoMoreInteractions(update);
    }
}
