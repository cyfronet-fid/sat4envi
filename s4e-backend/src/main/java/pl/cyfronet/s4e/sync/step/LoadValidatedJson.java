/*
 * Copyright 2020 ACC Cyfronet AGH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

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
