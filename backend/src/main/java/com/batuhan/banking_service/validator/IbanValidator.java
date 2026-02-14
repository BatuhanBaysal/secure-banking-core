package com.batuhan.banking_service.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.math.BigInteger;

public class IbanValidator implements ConstraintValidator<ValidIban, String> {

    @Override
    public boolean isValid(String iban, ConstraintValidatorContext context) {
        if (iban == null || iban.isBlank()) return false;

        String cleanIban = iban.replace(" ", "").toUpperCase(java.util.Locale.ENGLISH);
        if (cleanIban.length() < 15 || cleanIban.length() > 34) return false;

        String rearranged = cleanIban.substring(4) + cleanIban.substring(0, 4);

        StringBuilder numericIban = new StringBuilder();
        for (char c : rearranged.toCharArray()) {
            if (c >= '0' && c <= '9') {
                numericIban.append(c);
            } else if (c >= 'A' && c <= 'Z') {
                numericIban.append(c - 'A' + 10);
            }
        }

        try {
            BigInteger ibanNumber = new BigInteger(numericIban.toString());
            int remainder = ibanNumber.mod(BigInteger.valueOf(97)).intValue();
            return remainder == 1;

        } catch (Exception e) {
            return false;
        }
    }
}