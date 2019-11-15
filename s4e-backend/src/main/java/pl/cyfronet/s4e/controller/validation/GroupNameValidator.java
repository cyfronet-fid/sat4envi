package pl.cyfronet.s4e.controller.validation;

import lombok.Data;
import pl.cyfronet.s4e.service.InstitutionService;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

@Data
public class GroupNameValidator implements ConstraintValidator<GroupNameValid, String> {
    private final InstitutionService institutionService;

    @Override
    public void initialize(GroupNameValid constraintAnnotation) {

    }

    @Override
    public boolean isValid(String s, ConstraintValidatorContext constraintValidatorContext) {
        if (s == null) return false;

        if (s.contains(institutionService.PREFIX) || s.contains(institutionService.DEFAULT))
            return false;

        return true;
    }
}
