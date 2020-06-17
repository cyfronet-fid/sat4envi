package pl.cyfronet.s4e.controller.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URLConnection;
import java.util.Base64;
import java.util.regex.Pattern;

public class ContentTypeValidator implements ConstraintValidator<ContentType, String> {
    private Pattern pattern;

    @Override
    public void initialize(ContentType constraintAnnotation) {
        pattern = Pattern.compile(constraintAnnotation.pattern());
    }

    public boolean isValid(String encoded, ConstraintValidatorContext context) {
        if (encoded == null) {
            return true;
        }

        try {
            byte[] bytes = Base64.getDecoder().decode(encoded);
            String contentType = URLConnection.guessContentTypeFromStream(new ByteArrayInputStream(bytes));
            return pattern.matcher(contentType).matches();
        } catch (IllegalArgumentException e) {
            return false;
        } catch (IOException e) {
            return false;
        }
    }
}
