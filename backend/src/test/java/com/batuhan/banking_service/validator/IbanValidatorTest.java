package com.batuhan.banking_service.validator;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * IBAN Validator Unit Tests
 * - Verifies Turkish and international IBAN format compliance.
 * - Ensures checksum (Mod 97) and length validations are functioning correctly.
 */
@DisplayName("Validator - IBAN Constraint Unit Tests")
class IbanValidatorTest {

    private final IbanValidator validator = new IbanValidator();

    @Test
    @DisplayName("Success: Valid Turkish IBAN should pass validation")
    void isValid_ValidIban_ReturnsTrue() {
        // GIVEN
        String validIban = "TR540006203562327707039762";

        // WHEN
        boolean result = validator.isValid(validIban, null);

        // THEN
        assertThat(result)
                .as("A correct Turkish IBAN should be validated successfully")
                .isTrue();
    }

    @ParameterizedTest(name = "Scenario: Invalid IBAN input [{0}]")
    @ValueSource(strings = {
            "",
            "   ",
            "INVALID-FORMAT",
            "TR000000000000000000000000",
            "TR123",
            "TR540006203562327707039762123"
    })
    @DisplayName("Failure: Invalid formats and checksums should return false")
    void isValid_InvalidIbans_ReturnsFalse(String invalidIban) {
        // GIVEN

        // WHEN
        boolean result = validator.isValid(invalidIban, null);

        // THEN
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("Failure: Null IBAN should return false and not throw exception")
    void isValid_NullIban_ReturnsFalse() {
        // GIVEN
        String nullIban = null;

        // WHEN
        boolean result = validator.isValid(nullIban, null);

        // THEN
        assertThat(result).isFalse();
    }

    @ParameterizedTest(name = "Scenario: {1}")
    @MethodSource("provideIbanScenarios")
    @DisplayName("Comprehensive: Multi-scenario IBAN validation (Spaces & Countries)")
    void isValid_VariousScenarios(String iban, String description, boolean expected) {
        // GIVEN

        // WHEN
        boolean result = validator.isValid(iban, null);

        // THEN
        assertThat(result)
                .as(description)
                .isEqualTo(expected);
    }

    // --- HELPERS ---
    private static Stream<Arguments> provideIbanScenarios() {
        return Stream.of(
                Arguments.of("TR540006203562327707039762", "Valid TR IBAN (Standard)", true),
                Arguments.of("TR54 0006 2035 6232 7707 0397 62", "Valid TR IBAN with spaces (Sanitization check)", true),
                Arguments.of("DE89370400440532013000", "Valid DE IBAN (International support)", true)
        );
    }
}