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

import javax.imageio.ImageIO;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Base64;

public class ImageDimensionsValidator implements ConstraintValidator<ImageDimensions, String> {
    private int maxWidth;
    private int maxHeight;

    @Override
    public void initialize(ImageDimensions constraintAnnotation) {
        maxWidth = constraintAnnotation.maxWidth();
        maxHeight = constraintAnnotation.maxHeight();
    }

    public boolean isValid(String encoded, ConstraintValidatorContext context) {
        if (encoded == null) {
            return true;
        }

        try {
            byte[] bytes = Base64.getDecoder().decode(encoded);
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(bytes));
            return image.getWidth() <= maxWidth && image.getHeight() <= maxHeight;
        } catch (IllegalArgumentException e) {
            return false;
        } catch (IOException e) {
            return false;
        }
    }
}
