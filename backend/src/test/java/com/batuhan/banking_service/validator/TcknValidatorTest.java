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
 * TCKN Validator Unit Tests
 * - Verifies the 11-digit algorithmic verification (Mod 10 and Mod 11).
 * - Ensures business rules (cannot start with zero, numeric only) are strictly enforced.
 */
@DisplayName("Validator - TCKN Algorithmic Unit Tests")
class TcknValidatorTest {

    private final TcknValidator validator = new TcknValidator();

    @Test
    @DisplayName("Success: Valid TCKN with correct checksums should pass")
    void isValid_CorrectChecksum_ReturnsTrue() {
        // GIVEN
        String validTckn = "10000000146";

        // WHEN
        boolean result = validator.isValid(validTckn, null);

        // THEN
        assertThat(result)
                .as("A mathematically correct TCKN should be validated successfully")
                .isTrue();
    }

    @ParameterizedTest(name = "Scenario: Invalid Input [{0}]")
    @ValueSource(strings = {
            "10000000147", // Wrong 11th digit
            "10000000156", // Wrong 10th digit
            "00000000146", // Cannot start with zero
            "12345",       // Too short
            "123456789012",// Too long
            "1234567890A", // Non-numeric
            "",            // Empty
            "           "  // Blank
    })
    @DisplayName("Failure: Invalid TCKN formats or checksums should return false")
    void isValid_InvalidTckn_ReturnsFalse(String invalidTckn) {
        // GIVEN

        // WHEN
        boolean result = validator.isValid(invalidTckn, null);

        // THEN
        assertThat(result)
                .as("TCKN '%s' should be considered invalid", invalidTckn)
                .isFalse();
    }

    @Test
    @DisplayName("Failure: Null TCKN should return false and not throw exception")
    void isValid_NullTckn_ReturnsFalse() {
        // GIVEN
        String nullTckn = null;

        // WHEN
        boolean result = validator.isValid(nullTckn, null);

        // THEN
        assertThat(result).isFalse();
    }

    @ParameterizedTest(name = "Scenario: {1}")
    @MethodSource("provideSpecificScenarios")
    @DisplayName("Comprehensive: TCKN Business Rule Validation")
    void isValid_SpecificScenarios(String tckn, String description, boolean expected) {
        // GIVEN

        // WHEN
        boolean result = validator.isValid(tckn, null);

        // THEN
        assertThat(result)
                .as(description)
                .isEqualTo(expected);
    }

    // --- (HELPERS: Test Data Provider) ---
    private static Stream<Arguments> provideSpecificScenarios() {
        return Stream.of(
                Arguments.of("10000000146", "Standard Valid TCKN", true),
                Arguments.of("01234567890", "Invalid: Starts with zero check", false),
                Arguments.of("11111111111", "Algorithmic Check: Repeating digits failure", false)
        );
    }
}