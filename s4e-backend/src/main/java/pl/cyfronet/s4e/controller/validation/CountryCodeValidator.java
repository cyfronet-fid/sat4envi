package pl.cyfronet.s4e.controller.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Locale;

public class CountryCodeValidator implements ConstraintValidator<CountryCode, String> {
    public boolean isValid(String value, ConstraintValidatorContext context) {
        // Null is fine, but reject empty string.
        if (value == null) {
            return true;
        }

        return Locale.getISOCountries(Locale.IsoCountryCode.PART1_ALPHA2).contains(value);
    }
}
