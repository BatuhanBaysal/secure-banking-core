package com.batuhan.banking_service.repository.user;

import com.batuhan.banking_service.TestDataFactory;
import com.batuhan.banking_service.entity.UserEntity;
import com.batuhan.banking_service.repository.BaseIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Integration tests for UserRepository.
 * Validates user persistence, unique constraint enforcement (email, TCKN),
 * and retrieval logic by customer identifiers.
 * Also ensures that soft delete (active/passive) status transitions are correctly handled in the database.
 */
@DisplayName("User Repository Integration Tests")
class UserRepositoryTest extends BaseIntegrationTest {

    @Test
    @DisplayName("1. Success: Save and find user by customer number")
    void shouldSaveAndFindByCustomerNumber() {
        // Given
        UserEntity user = TestDataFactory.createTestUser();
        String customerNo = "CUS-777";
        user.setCustomerNumber(customerNo);

        // When
        userRepository.saveAndFlush(user);
        Optional<UserEntity> foundUser = userRepository.findByCustomerNumber(customerNo);

        // Then
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getCustomerNumber()).isEqualTo(customerNo);
    }

    @Test
    @DisplayName("2. Success: Return empty when user does not exist")
    void shouldReturnEmpty_WhenUserDoesNotExist() {
        // Given
        String nonExistentId = "NON-EXISTENT-ID";

        // When
        Optional<UserEntity> foundUser = userRepository.findByCustomerNumber(nonExistentId);

        // Then
        assertThat(foundUser).isEmpty();
    }

    @Test
    @DisplayName("3. Failure: Throw exception when email is not unique")
    void shouldThrowException_WhenEmailIsNotUnique() {
        // Given
        String duplicateEmail = "duplicate@test.com";

        UserEntity user1 = TestDataFactory.createTestUser();
        user1.setEmail(duplicateEmail);
        userRepository.saveAndFlush(user1);

        UserEntity user2 = TestDataFactory.createTestUser();
        user2.setEmail(duplicateEmail);
        user2.setCustomerNumber("DIFF-CUST-123");
        user2.setTckn("99999999901");

        // When & Then
        assertThrows(DataIntegrityViolationException.class, () -> {
            userRepository.saveAndFlush(user2);
        });
    }

    @Test
    @DisplayName("4. Success: Handle soft delete status transition")
    void shouldHandleSoftDeleteStatus() {
        // Given
        UserEntity user = saveTestUser();
        String customerNo = user.getCustomerNumber();

        // When
        user.setActive(false);
        userRepository.saveAndFlush(user);

        // Then
        Optional<UserEntity> updatedUser = userRepository.findByCustomerNumber(customerNo);
        assertThat(updatedUser).isPresent();
        assertThat(updatedUser.get().isActive()).isFalse();
    }
}