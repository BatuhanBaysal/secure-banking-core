package com.batuhan.banking_service.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;

public class TcknValidator implements ConstraintValidator<ValidTckn, String> {

    private static final String TCKN_REGEX = "^[1-9]\\d{10}$";
    private static final Pattern TCKN_PATTERN = Pattern.compile(TCKN_REGEX);

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.length() != 11 || !TCKN_PATTERN.matcher(value).matches()) {
            return false;
        }

        try {
            int[] digits = new int[11];
            for (int i = 0; i < 11; i++) {
                digits[i] = Character.getNumericValue(value.charAt(i));
            }

            int oddSum = digits[0] + digits[2] + digits[4] + digits[6] + digits[8];
            int evenSum = digits[1] + digits[3] + digits[5] + digits[7];

            int calculatedTenth = ((oddSum * 7) - evenSum) % 10;
            if (calculatedTenth < 0) {
                calculatedTenth += 10;
            }

            if (calculatedTenth != digits[9]) {
                return false;
            }

            int totalSum = 0;
            for (int i = 0; i < 10; i++) {
                totalSum += digits[i];
            }

            return (totalSum % 10) == digits[10];

        } catch (Exception e) {
            return false;
        }
    }
}