package com.batuhan.banking_service.service.transaction;

import com.batuhan.banking_service.TestDataFactory;
import com.batuhan.banking_service.entity.TransactionEntity;
import com.batuhan.banking_service.exception.BankingServiceException;
import com.batuhan.banking_service.service.BaseServiceTest;
import com.batuhan.banking_service.service.impl.TransactionServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("Transaction Service - Query Operations")
@MockitoSettings(strictness = Strictness.LENIENT)
class TransactionQueryTest extends BaseServiceTest {

    @InjectMocks
    private TransactionServiceImpl transactionService;

    @Nested
    @DisplayName("1. Transaction History Operations")
    class HistoryTests {

        @Test
        @DisplayName("Success: Get paginated transaction history for a valid owner")
        void getTransactionHistory_Success() {
            // Given
            String iban = TestDataFactory.VALID_IBAN;
            var account = TestDataFactory.createTestAccount(TestDataFactory.createTestUser(), iban);
            Pageable pageable = PageRequest.of(0, 10);

            // When
            mockAccountLookup(account);
            when(bankingBusinessValidator.isAccountOwner(iban)).thenReturn(true);
            when(transactionRepository.findAllByIban(eq(iban), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(Collections.emptyList()));

            var result = transactionService.getTransactionHistory(iban, pageable);

            // Then
            assertAll("History Result Checks",
                    () -> assertThat(result).isNotNull(),
                    () -> verify(transactionRepository).findAllByIban(eq(iban), any(Pageable.class)),
                    () -> verify(bankingBusinessValidator).isAccountOwner(iban)
            );
        }

        @Test
        @DisplayName("Failure: Block access to transaction history for unauthorized accounts")
        void getTransactionHistory_Unauthorized_Failure() {
            // Given
            String iban = "TR-UNAUTHORIZED";
            var account = TestDataFactory.createTestAccount(TestDataFactory.createTestUser(), iban);
            Pageable pageable = PageRequest.of(0, 10);

            // When
            mockAccountLookup(account);
            when(bankingBusinessValidator.isAccountOwner(iban)).thenReturn(false);

            // Then
            BankingServiceException ex = assertThrows(BankingServiceException.class,
                    () -> transactionService.getTransactionHistory(iban, pageable));

            assertThat(ex.getStatus()).isEqualTo(HttpStatus.FORBIDDEN);
            verify(transactionRepository, never()).findAllByIban(anyString(), any());
        }
    }

    @Nested
    @DisplayName("2. Transaction Filtering Operations")
    class FilterTests {

        @Test
        @DisplayName("Success: Filter transactions using dynamic criteria and pagination")
        @SuppressWarnings("unchecked")
        void filterTransactions_Success() {
            // Given
            String iban = TestDataFactory.VALID_IBAN;
            var account = TestDataFactory.createTestAccount(TestDataFactory.createTestUser(), iban);
            Pageable pageable = PageRequest.of(0, 10);

            // When
            mockAccountLookup(account);
            when(bankingBusinessValidator.isAccountOwner(iban)).thenReturn(true);
            when(transactionRepository.findAll(any(Specification.class), eq(pageable)))
                    .thenReturn(new PageImpl<>(Collections.emptyList()));

            var result = transactionService.filterTransactions(
                    iban, BigDecimal.ZERO, BigDecimal.TEN, null, null, pageable);

            // Then
            assertThat(result).isNotNull();
            verify(transactionRepository).findAll(any(Specification.class), eq(pageable));
        }

        @Test
        @DisplayName("Success: Retrieve all transactions for an IBAN using Specification-based list")
        @SuppressWarnings("unchecked")
        void getAllTransactionsByIban_Success() {
            // Given
            String iban = TestDataFactory.VALID_IBAN;
            var account = TestDataFactory.createTestAccount(TestDataFactory.createTestUser(), iban);
            var mockEntity = TestDataFactory.createTransactionEntity(account, account, BigDecimal.TEN);
            var mockResponse = TestDataFactory.createTransactionResponse();

            // When
            mockAccountLookup(account);
            when(bankingBusinessValidator.isAccountOwner(iban)).thenReturn(true);
            when(transactionRepository.findAll(any(Specification.class))).thenReturn(List.of(mockEntity));
            when(transactionMapper.toResponse(any(TransactionEntity.class))).thenReturn(mockResponse);

            var result = transactionService.getAllTransactionsByIban(iban);

            // Then
            var firstResponse = result.get(0);
            assertAll("List Specification Checks",
                    () -> assertThat(result).isNotNull().isNotEmpty(),
                    () -> assertThat(firstResponse).isNotNull(),
                    () -> assertThat(firstResponse.amount()).isEqualByComparingTo(new BigDecimal("100.00")),
                    () -> verify(transactionRepository).findAll(any(Specification.class))
            );
        }
    }
}