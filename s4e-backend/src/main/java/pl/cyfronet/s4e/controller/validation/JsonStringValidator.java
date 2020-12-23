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

package pl.cyfronet.s4e.controller.validation;

import jakarta.json.Json;
import jakarta.json.JsonReader;
import jakarta.json.stream.JsonParsingException;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

public class JsonStringValidator implements ConstraintValidator<JsonString, String> {
    public boolean isValid(String value, ConstraintValidatorContext context) {
        // Null is fine, but reject empty string.
        if (value == null) {
            return true;
        }

        try (JsonReader jsonReader = Json.createReader(new ByteArrayInputStream(value.getBytes(StandardCharsets.UTF_8)))) {
            jsonReader.read();
            return true;
        } catch (JsonParsingException e) {
            return false;
        }
    }
}
