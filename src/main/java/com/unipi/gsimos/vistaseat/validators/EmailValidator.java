package com.unipi.gsimos.vistaseat.validators;

import com.unipi.gsimos.vistaseat.customAnnotations.ValidEmail;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

// helper Class used for email validation with regex

public class EmailValidator implements ConstraintValidator<ValidEmail, String> {

    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.!#-]+@[A-Za-z0-9.-]+\\.[a-zA-Z]{2,}$";

    @Override
    public void initialize(ValidEmail constraintAnnotation) {

    }

    @Override
    public boolean isValid(String email, ConstraintValidatorContext constraintValidatorContext) {
        return email != null && email.matches(EMAIL_REGEX);
    }
}
