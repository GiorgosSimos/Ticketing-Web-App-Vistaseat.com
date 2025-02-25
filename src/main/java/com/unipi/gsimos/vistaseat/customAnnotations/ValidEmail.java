package com.unipi.gsimos.vistaseat.customAnnotations;


import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import org.hibernate.validator.internal.constraintvalidators.bv.EmailValidator;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = {EmailValidator.class})
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidEmail {
    String message() default "Invalid email address format";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
