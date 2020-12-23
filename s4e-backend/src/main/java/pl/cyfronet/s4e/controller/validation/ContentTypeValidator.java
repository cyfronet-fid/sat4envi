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
