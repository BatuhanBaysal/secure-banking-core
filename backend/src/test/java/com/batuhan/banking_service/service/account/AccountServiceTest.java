package com.batuhan.banking_service.service.account;

import com.batuhan.banking_service.TestDataFactory;
import com.batuhan.banking_service.dto.request.AccountCreateRequest;
import com.batuhan.banking_service.dto.response.AccountResponse;
import com.batuhan.banking_service.entity.AccountEntity;
import com.batuhan.banking_service.entity.UserEntity;
import com.batuhan.banking_service.entity.enums.AccountStatus;
import com.batuhan.banking_service.entity.enums.CurrencyType;
import com.batuhan.banking_service.exception.BankingServiceException;
import com.batuhan.banking_service.service.BaseServiceTest;
import com.batuhan.banking_service.service.impl.AccountServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AccountService business logic.
 * Manages the full lifecycle of a bank account, including creation with limit validation,
 * ownership security checks, and conditional closure rules.
 */
@DisplayName("Account Service - Lifecycle and Business Rule Tests")
class AccountServiceTest extends BaseServiceTest {

    @InjectMocks
    private AccountServiceImpl accountService;

    @Nested
    @DisplayName("1. Account Creation Processes")
    class CreationTests {

        @Test
        @DisplayName("Success: Create account when user is valid and under limit")
        void createAccount_WhenValid_ShouldSucceed() {
            // Given
            AccountCreateRequest request = TestDataFactory.createAccountRequest();
            UserEntity user = TestDataFactory.createTestUser();
            AccountEntity accountEntity = TestDataFactory.createTestAccount(user, "TR-MOCK-IBAN");
            AccountResponse mockResponse = new AccountResponse(
                    UUID.randomUUID(),
                    "TR-MOCK-IBAN",
                    BigDecimal.ZERO,
                    CurrencyType.TRY,
                    AccountStatus.ACTIVE,
                    request.customerNumber()
            );

            // When
            when(bankingBusinessValidator.validateAndGetCustomer(request.customerNumber())).thenReturn(user);
            when(accountMapper.toEntity(any())).thenReturn(accountEntity);
            when(accountRepository.save(any(AccountEntity.class))).thenReturn(accountEntity);
            when(accountMapper.toResponse(any())).thenReturn(mockResponse);

            AccountResponse response = accountService.createAccount(request);

            // Then
            assertAll("Account Creation Verification",
                    () -> assertThat(response).isNotNull(),
                    () -> assertThat(response.iban()).isEqualTo("TR-MOCK-IBAN"),
                    () -> verify(accountRepository).save(any(AccountEntity.class)),
                    () -> verify(bankingBusinessValidator).validateMaxAccountCount(user)
            );
        }

        @Test
        @DisplayName("Failure: Throw exception when maximum account limit is reached")
        void createAccount_WhenMaxLimitReached_ShouldThrowException() {
            // Given
            AccountCreateRequest request = TestDataFactory.createAccountRequest();
            UserEntity user = TestDataFactory.createTestUser();

            // When
            when(bankingBusinessValidator.validateAndGetCustomer(any())).thenReturn(user);
            doThrow(new BankingServiceException("Limit reached", HttpStatus.BAD_REQUEST))
                    .when(bankingBusinessValidator).validateMaxAccountCount(user);

            // Then
            assertThatThrownBy(() -> accountService.createAccount(request))
                    .isInstanceOf(BankingServiceException.class)
                    .hasMessageContaining("Limit reached");

            verify(accountRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("2. Account Security and Ownership")
    class SecurityTests {

        @Test
        @DisplayName("Failure: Throw Forbidden when unauthorized user accesses account")
        void getAccount_WhenNotOwner_ShouldThrowForbidden() {
            // Given
            String iban = "TR1";
            AccountEntity account = TestDataFactory.createTestAccount(TestDataFactory.createTestUser(), iban);

            // When
            when(accountRepository.findByIban(iban)).thenReturn(Optional.of(account));
            doThrow(new BankingServiceException("Forbidden", HttpStatus.FORBIDDEN))
                    .when(bankingBusinessValidator).validateOwnership(any(UserEntity.class));

            // Then
            assertThatThrownBy(() -> accountService.getAccountByIban(iban))
                    .isInstanceOf(BankingServiceException.class)
                    .matches(ex -> ((BankingServiceException) ex).getStatus() == HttpStatus.FORBIDDEN);
        }
    }

    @Nested
    @DisplayName("3. Account Termination Processes")
    class TerminationTests {

        @Test
        @DisplayName("Failure: Block closure if account has remaining balance")
        void closeAccount_WhenBalanceIsNotEmpty_ShouldThrowBadRequest() {
            // Given
            String iban = "TR1";
            AccountEntity account = TestDataFactory.createTestAccount(TestDataFactory.createTestUser(), iban);
            account.setBalance(BigDecimal.TEN);

            // When
            when(accountRepository.findByIban(iban)).thenReturn(Optional.of(account));

            // Then
            assertThatThrownBy(() -> accountService.closeAccount(iban))
                    .isInstanceOf(BankingServiceException.class)
                    .matches(ex -> ((BankingServiceException) ex).getStatus() == HttpStatus.BAD_REQUEST);

            verify(accountRepository, never()).save(any());
        }

        @Test
        @DisplayName("Success: Close account and set status to CLOSED when balance is zero")
        void closeAccount_WhenValid_ShouldSetStatusToClosed() {
            // Given
            UserEntity user = TestDataFactory.createTestUser();
            AccountEntity account = TestDataFactory.createEmptyBalanceAccount(user);

            // When
            when(accountRepository.findByIban(account.getIban())).thenReturn(Optional.of(account));
            when(accountRepository.save(any(AccountEntity.class))).thenReturn(account);

            accountService.closeAccount(account.getIban());

            // Then
            assertAll("Account Closure State Verification",
                    () -> assertThat(account.getStatus()).isEqualTo(AccountStatus.CLOSED),
                    () -> assertThat(account.isActive()).isFalse(),
                    () -> verify(accountRepository).save(account)
            );
        }
    }
}