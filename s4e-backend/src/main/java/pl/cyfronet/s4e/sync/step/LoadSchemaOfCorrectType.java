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

import jakarta.json.Json;
import jakarta.json.JsonException;
import jakarta.json.JsonReader;
import lombok.Builder;
import lombok.val;
import org.leadpony.justify.api.JsonSchema;
import org.leadpony.justify.api.JsonValidationService;
import pl.cyfronet.s4e.bean.Schema;
import pl.cyfronet.s4e.data.repository.SchemaRepository;
import pl.cyfronet.s4e.sync.Error;
import pl.cyfronet.s4e.sync.context.BaseContext;
import pl.cyfronet.s4e.sync.context.SchemaData;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static pl.cyfronet.s4e.sync.Error.*;

@Builder
public class LoadSchemaOfCorrectType<T extends BaseContext> implements Step<T, Error> {
    public static final String SCHEMA_PROPERTY = "schema";

    public interface SchemaProjection {
        Long getId();

        pl.cyfronet.s4e.bean.Schema.Type getType();

        String getContent();
    }

    private final Supplier<SchemaRepository> schemaRepository;
    private final Supplier<JsonValidationService> jsonValidationService;

    private final Function<T, String> content;
    private final Function<T, Schema.Type> requiredType;
    private final BiConsumer<T, SchemaData> update;

    public Error apply(T context) {
        val error = context.getError();
        val schemaRepository = this.schemaRepository.get();
        val jsonValidationService = this.jsonValidationService.get();

        String content = this.content.apply(context);
        Schema.Type requiredType = this.requiredType.apply(context);

        String schemaName;
        try (JsonReader reader = Json.createReader(new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)))) {
            // Extract schema name from content.
            schemaName = reader.readObject().getString(SCHEMA_PROPERTY);
        } catch (JsonException e) {
            return error.code(ERR_INVALID_JSON).cause(e).build();
        } catch (NullPointerException | ClassCastException e) {
            return error.code(ERR_NO_SCHEMA_PROPERTY).cause(e).build();
        }
        // Find the corresponding Schema in db.
        SchemaProjection schemaProjection = schemaRepository.findByName(schemaName, SchemaProjection.class).orElse(null);
        if (schemaProjection == null) {
            return error.code(ERR_SCHEMA_NOT_FOUND)
                    .parameter("schema_name", schemaName).build();
        }
        // Check if Schema type matches requiredType.
        if (schemaProjection.getType() != requiredType) {
            return error.code(ERR_SCHEMA_WRONG_TYPE)
                    .parameter("type_expected", requiredType)
                    .parameter("type_found", schemaProjection.getType()).build();
        }
        // Read the schema into JsonSchema.
        JsonSchema schemaJson = jsonValidationService.readSchema(
                new ByteArrayInputStream(schemaProjection.getContent().getBytes(StandardCharsets.UTF_8)),
                StandardCharsets.UTF_8
        );
        update.accept(context, new SchemaData(schemaName, schemaJson));
        return null;
    }
}
