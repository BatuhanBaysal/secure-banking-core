package com.batuhan.banking_service.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = TcknValidator.class)
@Documented
public @interface ValidTckn {

    String message() default "Invalid TCKN format or checksum";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}