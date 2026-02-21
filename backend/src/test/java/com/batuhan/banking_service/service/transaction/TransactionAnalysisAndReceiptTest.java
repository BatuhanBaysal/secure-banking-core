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
import org.mockito.InjectMocks;
import org.springframework.http.HttpStatus;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.*;

/**
 * Unit tests for TransactionService focusing on analytics and document operations.
 * Covers dashboard summaries, category spending analysis, and PDF receipt generation.
 * Ensures strict security checks for account ownership and transaction visibility.
 */
@DisplayName("Transaction Service - Analysis & Receipt Operations")
class TransactionAnalysisAndReceiptTest extends BaseServiceTest {

    @InjectMocks
    private TransactionServiceImpl transactionService;

    @Nested
    @DisplayName("1. Dashboard & Spending Analysis")
    class AnalysisTests {

        @Test
        @DisplayName("Success: Get dashboard summary with valid account ownership")
        void getDashboardSummary_Success() {
            // Given
            String iban = TestDataFactory.VALID_IBAN;
            var account = TestDataFactory.createTestAccount(TestDataFactory.createTestUser(), iban);

            // When
            mockAccountLookup(account);
            when(bankingBusinessValidator.isAccountOwner(iban)).thenReturn(true);
            when(transactionRepository.getTransactionSummary(iban))
                    .thenReturn(TestDataFactory.createTransactionSummaryDTO());

            var result = transactionService.getDashboardSummary(iban);

            // Then
            assertAll("Dashboard Summary Verification",
                    () -> assertThat(result).isNotNull(),
                    () -> assertThat(result.totalSent()).isNotNull(),
                    () -> verify(transactionRepository).getTransactionSummary(iban)
            );
        }

        @Test
        @DisplayName("Success: Get detailed category spending analysis")
        void getCategoryAnalysis_Success() {
            // Given
            String iban = TestDataFactory.VALID_IBAN;
            var account = TestDataFactory.createTestAccount(TestDataFactory.createTestUser(), iban);

            // When
            mockAccountLookup(account);
            when(bankingBusinessValidator.isAccountOwner(iban)).thenReturn(true);
            when(analysisService.calculateCategoryAnalysis(iban))
                    .thenReturn(List.of(TestDataFactory.createTransactionCategoryDTO("Food", BigDecimal.ONE, 100.0)));

            var result = transactionService.getCategoryAnalysis(iban);

            // Then
            assertThat(result).isNotEmpty();
            assertThat(result.get(0).category()).isEqualTo("Food");
            verify(analysisService).calculateCategoryAnalysis(iban);
        }
    }

    @Nested
    @DisplayName("2. Receipt Operations")
    class ReceiptTests {

        @Test
        @DisplayName("Success: Generate PDF receipt for an authorized transaction")
        void generateReceipt_Success() throws Exception {
            // Given
            Long txId = 1L;
            TransactionEntity tx = TransactionEntity.builder().id(txId).build();

            // When
            when(transactionRepository.findById(txId)).thenReturn(Optional.of(tx));
            when(bankingBusinessValidator.isTransactionOwner(txId)).thenReturn(true);
            when(pdfService.generateTransactionReceipt(tx))
                    .thenReturn(new ByteArrayInputStream("PDF_CONTENT".getBytes()));

            byte[] result = transactionService.generateTransactionReceipt(txId);

            // Then
            assertThat(result).isNotEmpty();
            verify(bankingBusinessValidator).isTransactionOwner(txId);
            verify(pdfService).generateTransactionReceipt(tx);
        }

        @Test
        @DisplayName("Failure: Block receipt generation when user is not the transaction owner")
        void generateReceipt_Unauthorized_Failure() {
            // Given
            Long txId = 99L;
            var owner = TestDataFactory.createTestUser();
            owner.setEmail("owner@test.com");

            var tx = TestDataFactory.createTransactionEntity(
                    TestDataFactory.createTestAccount(owner, TestDataFactory.VALID_IBAN),
                    TestDataFactory.createTestAccount(owner, TestDataFactory.OTHER_VALID_IBAN),
                    BigDecimal.TEN
            );

            // When
            mockCurrentUser("hacker@test.com");
            when(transactionRepository.findById(txId)).thenReturn(Optional.of(tx));

            // Then
            assertThatThrownBy(() -> transactionService.generateTransactionReceipt(txId))
                    .isInstanceOf(BankingServiceException.class)
                    .hasMessageContaining("not authorized");
        }
    }

    @Nested
    @DisplayName("3. Transfer Business Rule Failures")
    class TransferFailureTests {

        @Test
        @DisplayName("Failure: Block transfer when sender and receiver IBANs are the same")
        void transferMoney_SelfTransfer_Failure() {
            // Given
            TransactionRequest req = new TransactionRequest(
                    "TR-SAME", "TR-SAME", BigDecimal.TEN,
                    CurrencyType.TRY, "Self transfer attempt"
            );

            // When & Then
            assertThatThrownBy(() -> transactionService.transferMoney(req))
                    .isInstanceOf(BankingServiceException.class);

            verifyNoInteractions(accountRepository);
        }

        @Test
        @DisplayName("Failure: Throw exception when source account is inactive")
        void transferMoney_Inactive_Failure() {
            // Given
            String sender = TestDataFactory.VALID_IBAN;
            String receiver = TestDataFactory.OTHER_VALID_IBAN;
            TransactionRequest req = new TransactionRequest(sender, receiver, BigDecimal.TEN, CurrencyType.TRY, "Test");

            // When
            mockAccountPairLookup(sender, receiver);
            mockBusinessException("Inactive account", HttpStatus.BAD_REQUEST);

            // Then
            assertThatThrownBy(() -> transactionService.transferMoney(req))
                    .hasMessageContaining("Inactive account");
        }
    }
}