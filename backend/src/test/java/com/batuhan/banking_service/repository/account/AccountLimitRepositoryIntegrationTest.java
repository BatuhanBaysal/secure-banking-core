package com.batuhan.banking_service.repository.account;

import com.batuhan.banking_service.TestDataFactory;
import com.batuhan.banking_service.entity.AccountEntity;
import com.batuhan.banking_service.entity.AccountLimitEntity;
import com.batuhan.banking_service.repository.BaseIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for AccountLimitRepository.
 * Verifies the persistence and retrieval of daily account usage limits.
 * Ensures correct querying by account identifiers and specific dates within the database.
 */
@DisplayName("Account Limit Repository - Integration Tests")
class AccountLimitRepositoryIntegrationTest extends BaseIntegrationTest {

    @Test
    @DisplayName("Should find account limit by accountId and date")
    void findByAccountIdAndLimitDate_Success() {
        // Given
        String validRandomIban = TestDataFactory.generateRandomValidIban();
        AccountEntity account = createAndSaveAccount(validRandomIban, "1000.00");
        LocalDate today = LocalDate.now();

        AccountLimitEntity limit = AccountLimitEntity.builder()
                .account(account)
                .usedAmount(new BigDecimal("500.00"))
                .dailyLimit(account.getDailyLimit())
                .limitDate(today)
                .active(true)
                .build();

        // When
        accountLimitRepository.saveAndFlush(limit);
        Optional<AccountLimitEntity> result = accountLimitRepository.findByAccountIdAndLimitDate(account.getId(), today);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getUsedAmount()).isEqualByComparingTo("500.00");
        assertThat(result.get().getAccount().getIban()).isEqualTo(validRandomIban);
    }

    @Test
    @DisplayName("Should return empty when no limit exists for date")
    void findByAccountIdAndLimitDate_Empty() {
        // Given
        String validRandomIban = TestDataFactory.generateRandomValidIban();
        AccountEntity account = createAndSaveAccount(validRandomIban, "1000.00");
        LocalDate futureDate = LocalDate.now().plusYears(5);

        // When
        Optional<AccountLimitEntity> result = accountLimitRepository
                .findByAccountIdAndLimitDate(account.getId(), futureDate);

        // Then
        assertThat(result).isEmpty();
    }
}