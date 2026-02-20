package com.batuhan.banking_service.controller.transaction;

import com.batuhan.banking_service.TestDataFactory;
import com.batuhan.banking_service.config.SecurityConfig;
import com.batuhan.banking_service.controller.BaseControllerTest;
import com.batuhan.banking_service.controller.TransactionController;
import com.batuhan.banking_service.dto.request.TransactionRequest;
import com.batuhan.banking_service.dto.response.TransactionResponse;
import com.batuhan.banking_service.entity.enums.CurrencyType;
import com.batuhan.banking_service.exception.BankingServiceException;
import io.github.resilience4j.ratelimiter.RateLimiter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for the core transaction operations in TransactionController.
 * Covers money transfers, transaction history retrieval, and validation logic.
 * Ensures security constraints, rate limiting bypass, and proper error handling for business exceptions.
 */
@WebMvcTest(TransactionController.class)
@Import(SecurityConfig.class)
@DisplayName("Transaction Controller - Integration Tests")
class TransactionCoreTest extends BaseControllerTest {

    private static final String BASE_PATH = "/api/v1/transactions";

    @BeforeEach
    void setupMocks() {
        // Given
        RateLimiter mockRateLimiter = mock(RateLimiter.class);

        // When
        lenient().when(bankingBusinessValidator.isAccountOwner(anyString())).thenReturn(true);
        lenient().when(bankingBusinessValidator.isTransactionOwner(anyLong())).thenReturn(true);
        lenient().when(rateLimiterRegistry.rateLimiter(anyString())).thenReturn(mockRateLimiter);
        lenient().when(mockRateLimiter.acquirePermission()).thenReturn(true);
    }

    @Test
    @DisplayName("Transfer Money - Success")
    void transferMoney_Success() throws Exception {
        // Given
        TransactionRequest request = TestDataFactory.createTransactionRequest(
                TestDataFactory.VALID_IBAN,
                TestDataFactory.OTHER_VALID_IBAN,
                new BigDecimal("100.00")
        );
        TransactionResponse response = TestDataFactory.createTransactionResponse();

        // When
        when(transactionService.transferMoney(any(TransactionRequest.class))).thenReturn(response);

        // Then
        mockMvc.perform(post(BASE_PATH + "/transfer")
                        .with(csrf())
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.senderIban").value(TestDataFactory.VALID_IBAN));
    }

    @Test
    @DisplayName("POST /transfer - Unauthorized User (403)")
    void transferMoney_Unauthorized() throws Exception {
        // Given
        TransactionRequest request = TestDataFactory.createTransactionRequest(
                TestDataFactory.VALID_IBAN,
                TestDataFactory.OTHER_VALID_IBAN,
                BigDecimal.TEN
        );

        // When
        when(bankingBusinessValidator.isAccountOwner(anyString())).thenReturn(false);

        // Then
        mockMvc.perform(post(BASE_PATH + "/transfer")
                        .with(csrf())
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /transfer - Insufficient Funds (400)")
    void transferMoney_InsufficientFunds() throws Exception {
        // Given
        TransactionRequest request = TestDataFactory.createTransactionRequest(
                TestDataFactory.VALID_IBAN,
                TestDataFactory.OTHER_VALID_IBAN,
                BigDecimal.TEN
        );

        // When
        when(transactionService.transferMoney(any()))
                .thenThrow(new BankingServiceException("Insufficient balance", HttpStatus.BAD_REQUEST));

        // Then
        mockMvc.perform(post(BASE_PATH + "/transfer")
                        .with(csrf())
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Insufficient balance"));
    }

    @Test
    @DisplayName("Get History - Success")
    void getHistory_Success() throws Exception {
        // Given
        String iban = TestDataFactory.VALID_IBAN;

        // When
        when(transactionService.getTransactionHistory(eq(iban), any())).thenReturn(new PageImpl<>(List.of()));

        // Then
        mockMvc.perform(get(BASE_PATH + "/history/{iban}", iban)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /transfer - Validation Error (400) - Zero Amount")
    void transferMoney_ValidationError() throws Exception {
        // Given
        TransactionRequest invalidRequest = new TransactionRequest(
                TestDataFactory.VALID_IBAN,
                TestDataFactory.OTHER_VALID_IBAN,
                BigDecimal.ZERO,
                CurrencyType.TRY,
                "Zero amount transfer"
        );

        // When & Then
        mockMvc.perform(post(BASE_PATH + "/transfer")
                        .with(csrf())
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"));
    }
}