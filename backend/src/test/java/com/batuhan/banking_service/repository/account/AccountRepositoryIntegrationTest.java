package com.batuhan.banking_service.repository.account;

import com.batuhan.banking_service.entity.AccountEntity;
import com.batuhan.banking_service.repository.BaseIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for AccountRepository.
 * Verifies database operations including IBAN-based lookups, pessimistic locking for concurrency control,
 * and relational mapping checks between Accounts and Users.
 */
@DisplayName("Account Repository - Integration Tests")
class AccountRepositoryIntegrationTest extends BaseIntegrationTest {

    @Test
    @DisplayName("Should find account by IBAN and fetch User eagerly")
    void findByIban_Success() {
        // Given
        String iban = "TR001122334455667788990001";
        createAndSaveAccount(iban, "1500.00");
        accountRepository.flush();

        // When
        Optional<AccountEntity> result = accountRepository.findByIban(iban);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getIban()).isEqualTo(iban);
        assertThat(result.get().getUser()).isNotNull();
    }

    @Test
    @DisplayName("Should verify account existence by IBAN")
    void existsByIban_Success() {
        // Given
        String iban = "TR001122334455667788990011";
        createAndSaveAccount(iban, "100.00");
        accountRepository.flush();

        // When
        boolean exists = accountRepository.existsByIban(iban);

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Should find account with Pessimistic Write Lock")
    @Transactional
    void findByIbanWithLock_Success() {
        // Given
        String iban = "TR001122334455667788990022";
        createAndSaveAccount(iban, "2000.00");
        accountRepository.flush();

        // When
        Optional<AccountEntity> lockedAccount = accountRepository.findByIbanWithLock(iban);

        // Then
        assertThat(lockedAccount).isPresent();
        assertThat(lockedAccount.get().getIban()).isEqualTo(iban);
    }

    @Test
    @DisplayName("Should find accounts by customer number")
    void findByUserCustomerNumber_Success() {
        // Given
        String iban = "TR-FIND-TEST-1";
        AccountEntity account = createAndSaveAccount(iban, "100.00");
        String customerNo = account.getUser().getCustomerNumber();
        accountRepository.flush();

        // When
        List<AccountEntity> accounts = accountRepository.findByUserCustomerNumber(customerNo);

        // Then
        assertThat(accounts).isNotEmpty();
        assertThat(accounts.get(0).getUser().getCustomerNumber()).isEqualTo(customerNo);
    }
}