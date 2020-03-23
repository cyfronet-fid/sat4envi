package pl.cyfronet.s4e.controller.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = JsonStringValidator.class)
public @interface JsonString {
    String message() default "{pl.cyfronet.s4e.controller.validation.JsonString.message}";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
