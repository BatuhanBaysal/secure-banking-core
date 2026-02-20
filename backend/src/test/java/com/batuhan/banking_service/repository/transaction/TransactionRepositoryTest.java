package com.batuhan.banking_service.repository.transaction;

import com.batuhan.banking_service.TestDataFactory;
import com.batuhan.banking_service.dto.common.TransactionSummaryDTO;
import com.batuhan.banking_service.dto.common.WeeklyTrendDTO;
import com.batuhan.banking_service.entity.AccountEntity;
import com.batuhan.banking_service.entity.TransactionEntity;
import com.batuhan.banking_service.repository.BaseIntegrationTest;
import com.batuhan.banking_service.repository.specification.TransactionSpecifications;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for TransactionRepository and JPA Specifications.
 * Verifies custom JPQL queries for analytics (trends, summaries) and dynamic filtering logic.
 * Ensures that date-based calculations and account-specific transaction tracking work correctly at the database level.
 */
@DisplayName("Transaction Repository & Specification Integration Tests")
class TransactionRepositoryTest extends BaseIntegrationTest {

    private AccountEntity senderAccount;
    private AccountEntity receiverAccount;
    private final LocalDateTime now = LocalDateTime.now().withNano(0);

    @BeforeEach
    void setUp() {
        // Given
        clearDatabase();
        senderAccount = createAndSaveAccount(TestDataFactory.VALID_IBAN, "5000.00");
        receiverAccount = createAndSaveAccount(TestDataFactory.OTHER_VALID_IBAN, "1000.00");

        // When
        prepareTestData();
    }

    private void prepareTestData() {
        TransactionEntity t1 = TestDataFactory.createTransactionEntity(senderAccount, receiverAccount, new BigDecimal("200.00"));
        t1.setDescription("Market");

        TransactionEntity t2 = TestDataFactory.createTransactionEntity(receiverAccount, senderAccount, new BigDecimal("150.00"));
        t2.setDescription("Refund");

        TransactionEntity t3 = TestDataFactory.createTransactionEntity(senderAccount, receiverAccount, new BigDecimal("500.00"));
        t3.setDescription("OldRent");

        transactionRepository.saveAllAndFlush(List.of(t1, t2, t3));

        updateCreatedAt("transactions", "Market", now.minusDays(1));
        updateCreatedAt("transactions", "Refund", now.minusDays(2));
        updateCreatedAt("transactions", "OldRent", now.minusDays(15));

        transactionRepository.flush();
    }

    @Nested
    @DisplayName("Query Method Tests")
    class QueryMethodTests {

        @Test
        @DisplayName("Trend: Should exclude 15-day-old transaction")
        @Transactional(readOnly = true)
        void shouldFilterWeeklyTrendByDate() {
            // Given
            LocalDateTime sevenDaysAgo = now.minusDays(7);

            // When
            List<WeeklyTrendDTO> trend = transactionRepository.getWeeklySpendingTrend(senderAccount.getIban(), sevenDaysAgo);

            // Then
            assertThat(trend).isNotEmpty();
            assertThat(trend.get(0).totalAmount()).isEqualByComparingTo(new BigDecimal("200.00"));
        }

        @Test
        @DisplayName("Summary: Correct Sent/Received Totals")
        @Transactional(readOnly = true)
        void shouldGetTransactionSummary() {
            // When
            TransactionSummaryDTO summary = transactionRepository.getTransactionSummary(senderAccount.getIban());

            // Then
            assertThat(summary).isNotNull();
            assertThat(summary.totalSent()).isEqualByComparingTo(new BigDecimal("700.00"));
            assertThat(summary.totalReceived()).isEqualByComparingTo(new BigDecimal("150.00"));
            assertThat(summary.transactionCount()).isEqualTo(3L);
        }
    }

    @Nested
    @DisplayName("Specification Tests")
    class SpecificationTests {

        @Test
        @DisplayName("Spec: Date range filter (Strict)")
        @Transactional(readOnly = true)
        void shouldFilterByDateRange() {
            // Given
            LocalDateTime start = now.minusDays(5);
            LocalDateTime end = now.plusDays(1);

            // When
            Specification<TransactionEntity> spec = TransactionSpecifications.dateBetween(start, end);
            List<TransactionEntity> results = transactionRepository.findAll(spec);

            // Then
            assertThat(results).hasSize(2);
            assertThat(results).extracting(TransactionEntity::getDescription)
                    .containsExactlyInAnyOrder("Market", "Refund");
        }
    }
}