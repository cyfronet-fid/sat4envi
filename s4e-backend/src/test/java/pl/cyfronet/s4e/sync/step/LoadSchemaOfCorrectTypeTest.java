package pl.cyfronet.s4e.sync.step;

import jakarta.json.JsonException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.leadpony.justify.api.JsonSchema;
import org.leadpony.justify.api.JsonValidationService;
import org.mockito.Mock;
import pl.cyfronet.s4e.bean.Schema;
import pl.cyfronet.s4e.data.repository.SchemaRepository;
import pl.cyfronet.s4e.sync.Error;
import pl.cyfronet.s4e.sync.context.BaseContext;
import pl.cyfronet.s4e.sync.context.SchemaData;

import java.util.Optional;
import java.util.function.BiConsumer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.*;
import static pl.cyfronet.s4e.sync.Error.*;

class LoadSchemaOfCorrectTypeTest extends BaseStepTest<BaseContext> {
    @Mock
    private SchemaRepository schemaRepository;

    @Mock
    private JsonValidationService jsonValidationService;

    @Mock
    private BiConsumer<BaseContext, SchemaData> update;

    private String content;

    private LoadSchemaOfCorrectType step;

    @BeforeEach
    public void beforeEach() {
        stubContext();

        content = "{\"schema\":\"my_schema\"}";

        step = LoadSchemaOfCorrectType.builder()
                .schemaRepository(() -> schemaRepository)
                .jsonValidationService(() -> jsonValidationService)
                .content(c -> content)
                .requiredType(c -> Schema.Type.SCENE)
                .update(update)
                .build();
    }

    @Test
    public void shouldWork() {
        LoadSchemaOfCorrectType.SchemaProjection schemaProjection = mock(LoadSchemaOfCorrectType.SchemaProjection.class);

        when(schemaRepository.findByName("my_schema", LoadSchemaOfCorrectType.SchemaProjection.class))
                .thenReturn(Optional.of(schemaProjection));
        when(schemaProjection.getType()).thenReturn(Schema.Type.SCENE);
        String schemaContent = "{\"some_schema\":\"json\"}";
        when(schemaProjection.getContent()).thenReturn(schemaContent);
        JsonSchema jsonSchema = JsonSchema.EMPTY;
        when(jsonValidationService.readSchema(any(), any())).thenReturn(jsonSchema);

        Error error = step.apply(context);

        assertThat(error, is(nullValue()));
        verify(update).accept(context, new SchemaData("my_schema", jsonSchema));
        verifyNoMoreInteractions(update);
    }

    @Test
    public void shouldHandleInvalidJson() {
        content = "{\"schema:\"my_schema\"}";

        Error error = step.apply(context);

        assertThat(error, is(notNullValue()));
        assertThat(error.getCode(), is(equalTo(ERR_INVALID_JSON)));
        assertThat(error.getCause(), is(instanceOf(JsonException.class)));
        verifyNoMoreInteractions(update, schemaRepository, jsonValidationService);
    }

    @Test
    public void shouldHandleNoSchemaProperty() {
        content = "{\"not_schema\":\"not_value\"}";

        Error error = step.apply(context);

        assertThat(error, is(notNullValue()));
        assertThat(error.getCode(), is(equalTo(ERR_NO_SCHEMA_PROPERTY)));
        assertThat(error.getCause(), is(instanceOf(NullPointerException.class)));
        verifyNoMoreInteractions(update, schemaRepository, jsonValidationService);
    }

    @Test
    public void shouldHandleInvalidSchemaProperty() {
        content = "{\"schema\":42}";

        Error error = step.apply(context);

        assertThat(error, is(notNullValue()));
        assertThat(error.getCode(), is(equalTo(ERR_NO_SCHEMA_PROPERTY)));
        assertThat(error.getCause(), is(instanceOf(ClassCastException.class)));
        verifyNoMoreInteractions(update, schemaRepository, jsonValidationService);
    }

    @Test
    public void shouldHandleSchemaNotFound() {
        when(schemaRepository.findByName("my_schema", LoadSchemaOfCorrectType.SchemaProjection.class))
                .thenReturn(Optional.empty());

        Error error = step.apply(context);

        assertThat(error, is(notNullValue()));
        assertThat(error.getCode(), is(equalTo(ERR_SCHEMA_NOT_FOUND)));
        assertThat(error.getParameters(), hasEntry("schema_name", "my_schema"));
        verifyNoMoreInteractions(update, jsonValidationService);
    }

    @Test
    public void shouldHandleSchemaWrongType() {
        LoadSchemaOfCorrectType.SchemaProjection schemaProjection = mock(LoadSchemaOfCorrectType.SchemaProjection.class);

        when(schemaRepository.findByName("my_schema", LoadSchemaOfCorrectType.SchemaProjection.class))
                .thenReturn(Optional.of(schemaProjection));
        when(schemaProjection.getType()).thenReturn(Schema.Type.METADATA);

        Error error = step.apply(context);

        assertThat(error, is(notNullValue()));
        assertThat(error.getCode(), is(equalTo(ERR_SCHEMA_WRONG_TYPE)));
        assertThat(error.getParameters(), hasEntry("type_expected", Schema.Type.SCENE));
        assertThat(error.getParameters(), hasEntry("type_found", Schema.Type.METADATA));
        verifyNoMoreInteractions(update, jsonValidationService);
    }
}
