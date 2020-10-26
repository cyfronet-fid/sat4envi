package pl.cyfronet.s4e.controller.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = CountryCodeValidator.class)
public @interface CountryCode {
    String message() default "{pl.cyfronet.s4e.controller.validation.CountryCode.message}";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
