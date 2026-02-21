package com.batuhan.banking_service.service.transaction;

import com.batuhan.banking_service.TestDataFactory;
import com.batuhan.banking_service.dto.request.TransactionRequest;
import com.batuhan.banking_service.entity.TransactionEntity;
import com.batuhan.banking_service.entity.enums.CurrencyType;
import com.batuhan.banking_service.exception.BankingServiceException;
import com.batuhan.banking_service.service.BaseServiceTest;
import com.batuhan.banking_service.service.impl.TransactionServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for core money transfer operations in TransactionServiceImpl.
 * Verifies the integrity of double-entry accounting processes, business rule enforcement,
 * and concurrency management through alphabetical account locking to prevent deadlocks.
 */
@DisplayName("Transaction Service - Transfer Operations")
@MockitoSettings(strictness = Strictness.LENIENT)
class TransactionTransferTest extends BaseServiceTest {

    @InjectMocks
    private TransactionServiceImpl transactionService;

    @Nested
    @DisplayName("1. Successful Transfer Scenarios")
    class SuccessTests {

        @Test
        @DisplayName("Success: Normal money transfer between two valid accounts")
        void transferMoney_Successful() {
            // Given
            String senderIban = TestDataFactory.VALID_IBAN;
            String receiverIban = TestDataFactory.OTHER_VALID_IBAN;
            BigDecimal amount = new BigDecimal("100.00");
            var request = new TransactionRequest(senderIban, receiverIban, amount, CurrencyType.TRY, "Test Transfer");

            // When
            mockAccountPairLookup(senderIban, receiverIban);
            when(transactionRepository.save(any(TransactionEntity.class)))
                    .thenReturn(TestDataFactory.createTransactionEntity(null, null, amount));

            var mockRes = createMockTransactionResponse("TX-REF", amount, senderIban, receiverIban);
            when(transactionMapper.toResponse(any())).thenReturn(mockRes);

            var response = transactionService.transferMoney(request);

            // Then
            assertAll("Successful Transfer Checks",
                    () -> assertThat(response).isNotNull(),
                    () -> assertThat(response.referenceNumber()).isEqualTo("TX-REF"),
                    () -> verify(accountingManager).processAccounting(any(), any(), eq(amount)),
                    () -> verify(auditService).log(eq("MONEY_TRANSFER"), any(), contains("Reference:"))
            );
        }

        @Test
        @DisplayName("Success: Process should complete even if email notification fails")
        void transferMoney_EmailFailure_ShouldStillSucceed() {
            // Given
            String senderIban = TestDataFactory.VALID_IBAN;
            String receiverIban = TestDataFactory.OTHER_VALID_IBAN;
            var request = new TransactionRequest(senderIban, receiverIban, BigDecimal.TEN, CurrencyType.TRY, "Email Fail Test");

            // When
            mockAccountPairLookup(senderIban, receiverIban);
            when(transactionRepository.save(any())).thenReturn(new TransactionEntity());

            var mockRes = createMockTransactionResponse("REF", BigDecimal.TEN, senderIban, receiverIban);
            when(transactionMapper.toResponse(any())).thenReturn(mockRes);

            doThrow(new RuntimeException("SMTP Server Down"))
                    .when(emailService).sendTransferEmail(any(), any(), any(), any(), any());

            // Then
            assertDoesNotThrow(() -> transactionService.transferMoney(request));
            verify(accountingManager).processAccounting(any(), any(), any());
        }
    }

    @Nested
    @DisplayName("2. Business & Validation Failures")
    class FailureTests {

        @Test
        @DisplayName("Failure: Block transfer when sender and receiver IBANs are the same")
        void transferMoney_SelfTransfer_Failure() {
            // Given
            String sameIban = "TR-SAME";
            var request = new TransactionRequest(sameIban, sameIban, BigDecimal.TEN, CurrencyType.TRY, "Self");

            // When & Then
            assertThatThrownBy(() -> transactionService.transferMoney(request))
                    .isInstanceOf(BankingServiceException.class);

            verifyNoInteractions(accountRepository);
        }

        @Test
        @DisplayName("Failure: Return 404 Not Found when sender account record is missing")
        void transferMoney_SenderNotFound_Failure() {
            // Given
            var request = new TransactionRequest("MISSING", "TR-REC", BigDecimal.TEN, CurrencyType.TRY, "Fail");

            // When
            when(accountRepository.findByIbanWithLock("MISSING")).thenReturn(Optional.empty());

            // Then
            BankingServiceException ex = assertThrows(BankingServiceException.class,
                    () -> transactionService.transferMoney(request));
            assertThat(ex.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("3. Technical & Integrity Operations")
    class IntegrityTests {

        @Test
        @DisplayName("Integrity: Prevent Deadlocks by locking IBANs in alphabetical order")
        void transferMoney_ShouldLockInAlphabeticalOrder() {
            // Given
            String smallIban = "TR-AAA-111";
            String largeIban = "TR-ZZZ-999";
            var request = new TransactionRequest(largeIban, smallIban, BigDecimal.TEN, CurrencyType.TRY, "Integrity");

            // When
            mockAccountPairLookup(smallIban, largeIban);
            when(transactionRepository.save(any())).thenReturn(new TransactionEntity());

            var mockRes = createMockTransactionResponse("REF", BigDecimal.TEN, largeIban, smallIban);
            when(transactionMapper.toResponse(any())).thenReturn(mockRes);

            transactionService.transferMoney(request);

            // Then
            InOrder inOrder = inOrder(accountRepository);
            inOrder.verify(accountRepository).findByIbanWithLock(smallIban);
            inOrder.verify(accountRepository).findByIbanWithLock(largeIban);
        }
    }
}