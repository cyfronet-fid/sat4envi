package pl.cyfronet.s4e.sync.step;

import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import lombok.Builder;
import lombok.val;
import org.leadpony.justify.api.JsonSchema;
import org.leadpony.justify.api.JsonValidatingException;
import org.leadpony.justify.api.JsonValidationService;
import org.leadpony.justify.api.ProblemHandler;
import pl.cyfronet.s4e.sync.Error;
import pl.cyfronet.s4e.sync.context.BaseContext;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static pl.cyfronet.s4e.sync.Error.ERR_VIOLATES_SCHEMA;

@Builder
public class LoadValidatedJson<T extends BaseContext> implements Step<T, Error> {
    private final Supplier<JsonValidationService> jsonValidationService;

    private final Function<T, String> content;
    private final Function<T, JsonSchema> jsonSchema;
    private final BiConsumer<T, JsonObject> update;

    public Error apply(T context) {
        val error = context.getError();
        val jsonValidationService = this.jsonValidationService.get();

        String content = this.content.apply(context);
        JsonSchema jsonSchema = this.jsonSchema.apply(context);

        ProblemHandler problemHandler = ProblemHandler.throwing();
        try (
                JsonReader reader = jsonValidationService.createReader(
                        new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)),
                        StandardCharsets.UTF_8,
                        jsonSchema,
                        problemHandler)
        ) {
            update.accept(context, reader.readObject());
            return null;
        } catch (JsonValidatingException e) {
            return error.code(ERR_VIOLATES_SCHEMA).cause(e).build();
        }
    }
}
