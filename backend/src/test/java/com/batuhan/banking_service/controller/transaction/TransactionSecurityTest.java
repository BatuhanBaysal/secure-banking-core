package com.batuhan.banking_service.controller.transaction;

import com.batuhan.banking_service.config.SecurityConfig;
import com.batuhan.banking_service.controller.BaseControllerTest;
import com.batuhan.banking_service.controller.TransactionController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for security access controls in TransactionController.
 * Validates Method Security (@PreAuthorize) and SpEL expressions.
 * Ensures that users cannot access transaction data belonging to other accounts.
 */
@WebMvcTest(TransactionController.class)
@Import(SecurityConfig.class)
@DisplayName("Transaction - Security Controls")
@EnableMethodSecurity
class TransactionSecurityTest extends BaseControllerTest {

    @BeforeEach
    void setUp() {
        // Given

        // When
        when(bankingBusinessValidator.isAccountOwner(any())).thenReturn(true);
        when(bankingBusinessValidator.isTransactionOwner(anyLong())).thenReturn(true);
    }

    @Test
    @DisplayName("Forbidden - Accessing Other's Account")
    void forbidden_AccessUnauthorizedAccount() throws Exception {
        // Given
        String targetIban = "TR-NOT-MINE";

        // When
        when(bankingBusinessValidator.isAccountOwner(targetIban)).thenReturn(false);

        // Then
        mockMvc.perform(get("/api/v1/transactions/history/{iban}", targetIban)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());

        verify(bankingBusinessValidator).isAccountOwner(targetIban);
    }
}