package com.batuhan.banking_service.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = AgeValidator.class)
@Documented
public @interface MinAge {

    String message() default "User must be at least 18 years old";
    int value() default 18;
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}