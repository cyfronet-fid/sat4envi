package pl.cyfronet.s4e.controller.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class Base64Validator implements ConstraintValidator<Base64, String> {
   public boolean isValid(String encoded, ConstraintValidatorContext context) {
       if (encoded == null) {
           return true;
       }

       try {
           java.util.Base64.getDecoder().decode(encoded);
           return true;
       } catch (IllegalArgumentException e) {
           return false;
       }
   }
}
