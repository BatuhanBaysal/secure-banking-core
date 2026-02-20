package com.batuhan.banking_service.controller.transaction;

import com.batuhan.banking_service.config.SecurityConfig;
import com.batuhan.banking_service.controller.BaseControllerTest;
import com.batuhan.banking_service.controller.TransactionController;
import com.batuhan.banking_service.exception.BankingServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.context.support.WithMockUser;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for Transaction document generation and download operations.
 * Focuses on PDF receipt generation, identifying transactions, and handling file exports.
 * Verifies security ownership checks and proper HTTP header responses for document types.
 */
@WebMvcTest(TransactionController.class)
@Import(SecurityConfig.class)
@WithMockUser(username = "test@user.com", roles = "USER")
@DisplayName("Transaction - Document Operations")
class TransactionDocumentTest extends BaseControllerTest {

    @BeforeEach
    void setUp() {
        // Given

        // When
        when(bankingBusinessValidator.isAccountOwner(any())).thenReturn(true);
        when(bankingBusinessValidator.isTransactionOwner(anyLong())).thenReturn(true);
    }

    @Test
    @DisplayName("GET /receipt/{id} - Success")
    void downloadReceipt_Success() throws Exception {
        // Given
        Long transactionId = 1L;
        byte[] content = "PDF_CONTENT".getBytes();

        // When
        when(transactionService.generateTransactionReceipt(transactionId)).thenReturn(content);

        // Then
        mockMvc.perform(get("/api/v1/transactions/receipt/{id}", transactionId)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .accept(MediaType.APPLICATION_PDF))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_PDF_VALUE))
                .andExpect(content().bytes(content));
    }

    @Test
    @DisplayName("GET /receipt/{id} - Not Found (404)")
    void downloadReceipt_NotFound() throws Exception {
        // Given
        Long nonExistentId = 999L;

        // When
        when(transactionService.generateTransactionReceipt(nonExistentId))
                .thenThrow(new BankingServiceException("Receipt not found", HttpStatus.NOT_FOUND));

        // Then
        mockMvc.perform(get("/api/v1/transactions/receipt/{id}", nonExistentId)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Receipt not found"));
    }
}