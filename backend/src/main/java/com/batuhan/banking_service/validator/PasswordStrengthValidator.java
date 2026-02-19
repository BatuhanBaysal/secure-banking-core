package com.batuhan.banking_service.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;

public class PasswordStrengthValidator implements ConstraintValidator<ValidPassword, String> {

    private static final String STRENGTH_REGEX = "^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!*])(?=\\S+$).{8,}$";
    private static final Pattern VALIDATOR_PATTERN = Pattern.compile(STRENGTH_REGEX);

    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {
        if (password == null) {
            return false;
        }

        return VALIDATOR_PATTERN.matcher(password).matches();
    }
}