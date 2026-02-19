package com.batuhan.banking_service.validator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.LocalDate;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Age Validator Unit Tests
 * - Verifies the custom constraint logic for minimum age requirements.
 * - Ensures boundary conditions (exactly 18, 18-1 day, etc.) are handled correctly.
 */
@DisplayName("Validator - Age Constraint Unit Tests")
class AgeValidatorTest {

    private AgeValidator validator;

    @BeforeEach
    void setUp() {
        validator = new AgeValidator();
        MinAge minAgeAnnotation = mock(MinAge.class);
        when(minAgeAnnotation.value()).thenReturn(18);
        validator.initialize(minAgeAnnotation);
    }

    @Test
    @DisplayName("Success: User is exactly 18 years old today")
    void isValid_ExactlyEighteen_ReturnsTrue() {
        // GIVEN
        LocalDate exactlyEighteen = LocalDate.now().minusYears(18);

        // WHEN
        boolean result = validator.isValid(exactlyEighteen, null);

        // THEN
        assertThat(result).isTrue();
    }

    @ParameterizedTest(name = "Scenario: {1}")
    @MethodSource("provideValidDates")
    @DisplayName("Success: Various valid age scenarios")
    void isValid_ValidDates_ReturnsTrue(LocalDate date, String description) {
        // GIVEN

        // WHEN
        boolean result = validator.isValid(date, null);

        // THEN
        assertThat(result).as(description).isTrue();
    }

    @ParameterizedTest(name = "Scenario: {1}")
    @MethodSource("provideInvalidDates")
    @DisplayName("Failure: Various invalid age scenarios")
    void isValid_InvalidDates_ReturnsFalse(LocalDate date, String description) {
        // GIVEN

        // WHEN
        boolean result = validator.isValid(date, null);

        // THEN
        assertThat(result).as(description).isFalse();
    }

    @Test
    @DisplayName("Failure: Null birthdate should not pass validation")
    void isValid_NullDate_ReturnsFalse() {
        // GIVEN
        LocalDate nullDate = null;

        // WHEN
        boolean result = validator.isValid(nullDate, null);

        // THEN
        assertThat(result).isFalse();
    }

    // --- HELPERS ---
    private static Stream<Arguments> provideValidDates() {
        return Stream.of(
                Arguments.of(LocalDate.now().minusYears(18).minusDays(1), "18 years and 1 day old"),
                Arguments.of(LocalDate.now().minusYears(50), "Standard adult (50 years old)"),
                Arguments.of(LocalDate.of(1990, 1, 1), "Fixed historical date")
        );
    }

    private static Stream<Arguments> provideInvalidDates() {
        return Stream.of(
                Arguments.of(LocalDate.now().minusYears(18).plusDays(1), "1 day before 18th birthday"),
                Arguments.of(LocalDate.now().minusYears(17), "Exactly 17 years old"),
                Arguments.of(LocalDate.now().plusDays(1), "Future birth date")
        );
    }
}