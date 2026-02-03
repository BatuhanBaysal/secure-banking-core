package com.batuhan.banking_service.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TcknValidator implements ConstraintValidator<ValidTckn, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.length() != 11 || !value.matches("^[1-9][0-9]{10}$")) {
            log.debug("TCKN validation failed: Input '{}' does not match 11-digit numeric format or starts with zero", value);
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
            if (calculatedTenth < 0) calculatedTenth += 10;

            if (calculatedTenth != digits[9]) {
                log.warn("TCKN checksum failed for 10th digit. Expected: {}, Calculated: {}", digits[9], calculatedTenth);
                return false;
            }

            int totalSum = 0;
            for (int i = 0; i < 10; i++) {
                totalSum += digits[i];
            }

            int calculatedEleventh = totalSum % 10;
            if (calculatedEleventh != digits[10]) {
                log.warn("TCKN checksum failed for 11th digit. Expected: {}, Calculated: {}", digits[10], calculatedEleventh);
                return false;
            }

            log.info("TCKN validation successful for identifier ending with: ***{}", value.substring(8));
            return true;

        } catch (Exception e) {
            log.error("Unexpected error during TCKN validation process: {}", e.getMessage());
            return false;
        }
    }
}