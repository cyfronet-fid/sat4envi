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
