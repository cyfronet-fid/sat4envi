package pl.cyfronet.s4e.controller.validation;

import javax.json.Json;
import javax.json.JsonReader;
import javax.json.stream.JsonParsingException;
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
