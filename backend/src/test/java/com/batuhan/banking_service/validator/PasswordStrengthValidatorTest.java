package com.batuhan.banking_service.validator;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Password Strength Validator Unit Tests
 * - Verifies security policy compliance (length, mixed case, symbols, numbers).
 * - Ensures boundary cases like null or blank inputs are handled correctly.
 */
@DisplayName("Validator - Password Strength Unit Tests")
class PasswordStrengthValidatorTest {

    private final PasswordStrengthValidator validator = new PasswordStrengthValidator();

    @Test
    @DisplayName("Success: Password meets all complexity requirements")
    void isValid_StrongPassword_ReturnsTrue() {
        // GIVEN
        String strongPassword = "Password123!";

        // WHEN
        boolean result = validator.isValid(strongPassword, null);

        // THEN
        assertThat(result)
                .as("A strong password with mixed case, numbers, and special chars should be valid")
                .isTrue();
    }

    @ParameterizedTest(name = "Scenario: {1}")
    @MethodSource("provideWeakPasswords")
    @DisplayName("Failure: Passwords missing required elements should fail")
    void isValid_WeakPasswords_ReturnsFalse(String password, String failureReason) {
        // GIVEN

        // WHEN
        boolean result = validator.isValid(password, null);

        // THEN
        assertThat(result)
                .as("Failed scenario: " + failureReason)
                .isFalse();
    }

    @Test
    @DisplayName("Failure: Null or blank passwords should not pass validation")
    void isValid_EmptyScenarios_ReturnsFalse() {
        // GIVEN & WHEN & THEN
        assertThat(validator.isValid(null, null)).isFalse();
        assertThat(validator.isValid("", null)).isFalse();
        assertThat(validator.isValid("   ", null)).isFalse();
    }

    // --- HELPERS ---
    private static Stream<Arguments> provideWeakPasswords() {
        return Stream.of(
                Arguments.of("short1!", "Too short (less than 8 characters)"),
                Arguments.of("password123!", "Missing uppercase letter"),
                Arguments.of("PASSWORD123!", "Missing lowercase letter"),
                Arguments.of("Password!", "Missing numeric character"),
                Arguments.of("Password123", "Missing special character"),
                Arguments.of("Pass 123!", "Contains spaces"),
                Arguments.of("12345678", "Numeric only"),
                Arguments.of("!!!!!!!!", "Special characters only")
        );
    }
}