package com.batuhan.banking_service.controller.transaction;

import com.batuhan.banking_service.TestDataFactory;
import com.batuhan.banking_service.config.SecurityConfig;
import com.batuhan.banking_service.controller.BaseControllerTest;
import com.batuhan.banking_service.controller.TransactionController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.security.test.context.support.WithMockUser;

import java.io.ByteArrayInputStream;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for Transaction analytics and reporting endpoints.
 * Verifies dashboard summaries, data exports (Excel), and transaction filtering logic.
 * Ensures proper validation of query parameters and security constraints.
 */
@WebMvcTest(TransactionController.class)
@Import(SecurityConfig.class)
@WithMockUser(username = "test@user.com", roles = "USER")
@DisplayName("Transaction - Analytics & Reporting")
class TransactionAnalyticsTest extends BaseControllerTest {

    private static final String BASE_PATH = "/api/v1/transactions";

    @BeforeEach
    void setUp() {
        // Given

        // When
        when(bankingBusinessValidator.isAccountOwner(any())).thenReturn(true);
        when(bankingBusinessValidator.isTransactionOwner(anyLong())).thenReturn(true);
    }

    @Test
    @DisplayName("Dashboard Summary - Success")
    void getSummary_Success() throws Exception {
        // Given
        String iban = TestDataFactory.VALID_IBAN;

        // When
        when(bankingBusinessValidator.isAccountOwner(anyString())).thenReturn(true);
        when(transactionService.getDashboardSummary(iban)).thenReturn(TestDataFactory.createTransactionSummaryDTO());

        // Then
        mockMvc.perform(get(BASE_PATH + "/dashboard/summary").param("iban", iban))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Export Excel - Success")
    void exportExcel_Success() throws Exception {
        // Given
        String iban = TestDataFactory.VALID_IBAN;
        byte[] mockContent = "data".getBytes();

        // When
        when(bankingBusinessValidator.isAccountOwner(anyString())).thenReturn(true);
        when(excelService.transactionsToExcel(any())).thenReturn(new ByteArrayInputStream(mockContent));

        // Then
        mockMvc.perform(get(BASE_PATH + "/download/excel").param("iban", iban))
                .andExpect(status().isOk())
                .andExpect(header().exists(HttpHeaders.CONTENT_DISPOSITION));
    }

    @Test
    @DisplayName("GET /filter - Invalid Date Format (400)")
    void filterTransactions_InvalidDate() throws Exception {
        // Given
        String iban = TestDataFactory.VALID_IBAN;
        String invalidDate = "2024-99-99";

        // When & Then
        mockMvc.perform(get(BASE_PATH + "/filter")
                        .param("iban", iban)
                        .param("startDate", invalidDate))
                .andExpect(status().isBadRequest());
    }
}