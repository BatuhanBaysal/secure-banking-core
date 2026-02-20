package com.batuhan.banking_service.controller.account;

import com.batuhan.banking_service.TestDataFactory;
import com.batuhan.banking_service.config.SecurityConfig;
import com.batuhan.banking_service.controller.AccountController;
import com.batuhan.banking_service.controller.BaseControllerTest;
import com.batuhan.banking_service.dto.request.AccountCreateRequest;
import com.batuhan.banking_service.dto.response.AccountResponse;
import com.batuhan.banking_service.entity.enums.AccountStatus;
import com.batuhan.banking_service.entity.enums.CurrencyType;
import com.batuhan.banking_service.exception.BankingServiceException;
import io.github.resilience4j.ratelimiter.RateLimiter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for the AccountController.
 * Verifies API endpoints for account creation, retrieval, and closing.
 * Tests security roles, SpEL-based authorizations, and exception handling.
 */
@WebMvcTest(AccountController.class)
@Import(SecurityConfig.class)
@DisplayName("Account Controller - Full API Integration Tests")
class AccountControllerTest extends BaseControllerTest {

    private static final String BASE_URL = "/api/v1/accounts";
    private static final String ROLE_ADMIN = "ROLE_ADMIN";
    private static final String ROLE_USER = "ROLE_USER";

    @BeforeEach
    void setupCommonMocks() {
        // Given
        RateLimiter mockRateLimiter = mock(RateLimiter.class);

        // When
        lenient().when(rateLimiterRegistry.rateLimiter(anyString())).thenReturn(mockRateLimiter);
        lenient().when(mockRateLimiter.acquirePermission()).thenReturn(true);
        lenient().when(bankingBusinessValidator.isOwner(anyString())).thenReturn(true);
        lenient().when(bankingBusinessValidator.isAccountOwner(anyString())).thenReturn(true);
    }

    @Nested
    @DisplayName("1. Account Creation (POST)")
    class CreationTests {

        @Test
        @DisplayName("Success: Admin can create any account")
        void createAccount_AsAdmin_ReturnsCreated() throws Exception {
            // Given
            AccountCreateRequest request = TestDataFactory.createAccountRequest();
            AccountResponse response = new AccountResponse(UUID.randomUUID(), "TR123", BigDecimal.ZERO, CurrencyType.TRY, AccountStatus.ACTIVE, request.customerNumber());

            // When
            when(accountService.createAccount(any())).thenReturn(response);

            // Then
            mockMvc.perform(post(BASE_URL)
                            .with(csrf())
                            .with(jwt().authorities(new SimpleGrantedAuthority(ROLE_ADMIN)))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(header().exists("Location"))
                    .andExpect(jsonPath("$.data.iban").value("TR123"));
        }

        @Test
        @DisplayName("Success: Owner can create their own account")
        void createAccount_AsOwner_ReturnsCreated() throws Exception {
            // Given
            AccountCreateRequest request = TestDataFactory.createAccountRequest();
            AccountResponse response = new AccountResponse(UUID.randomUUID(), "TR-OWN", BigDecimal.ZERO, CurrencyType.TRY, AccountStatus.ACTIVE, request.customerNumber());

            // When
            when(accountService.createAccount(any())).thenReturn(response);
            when(bankingBusinessValidator.isOwner(request.customerNumber())).thenReturn(true);

            // Then
            mockMvc.perform(post(BASE_URL)
                            .with(csrf())
                            .with(jwt().jwt(j -> j.claim("sub", "test-user"))
                                    .authorities(new SimpleGrantedAuthority(ROLE_USER)))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated());
        }

        @Test
        @DisplayName("Failure: User cannot create account for others (403)")
        void createAccount_NotOwner_ReturnsForbidden() throws Exception {
            // Given
            AccountCreateRequest request = TestDataFactory.createAccountRequest();

            // When
            when(bankingBusinessValidator.isOwner(anyString())).thenReturn(false);

            // Then
            mockMvc.perform(post(BASE_URL)
                            .with(csrf())
                            .with(jwt().authorities(new SimpleGrantedAuthority(ROLE_USER)))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("2. Account Retrieval (GET)")
    class RetrievalTests {

        @Test
        @DisplayName("Success: Get account by IBAN as Admin")
        void getByIban_AsAdmin_ReturnsOk() throws Exception {
            // Given
            String iban = "TR99";
            AccountResponse response = new AccountResponse(UUID.randomUUID(), iban, BigDecimal.TEN, CurrencyType.TRY, AccountStatus.ACTIVE, "CUS1");

            // When
            when(accountService.getAccountByIban(anyString())).thenReturn(response);

            // Then
            mockMvc.perform(get(BASE_URL + "/{iban}", iban)
                            .with(jwt().authorities(new SimpleGrantedAuthority(ROLE_ADMIN))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.iban").value(iban));
        }

        @Test
        @DisplayName("Success: List customer accounts as Owner")
        void getByCustomer_AsOwner_ReturnsOk() throws Exception {
            // Given
            String customerNo = "CUS101";

            // When
            when(bankingBusinessValidator.isOwner(customerNo)).thenReturn(true);
            when(accountService.getAccountsByCustomerNumber(customerNo)).thenReturn(Collections.emptyList());

            // Then
            mockMvc.perform(get(BASE_URL + "/customer/{customerNumber}", customerNo)
                            .with(jwt().jwt(j -> j.claim("sub", "test-user"))
                                    .authorities(new SimpleGrantedAuthority(ROLE_USER))))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("3. Account Closing (DELETE)")
    class DeleteTests {

        @Test
        @DisplayName("Success: Admin can close any account")
        void closeAccount_AsAdmin_ReturnsOk() throws Exception {
            // Given
            String iban = "TR-CLOSE";

            // When
            doNothing().when(accountService).closeAccount(iban);

            // Then
            mockMvc.perform(delete(BASE_URL + "/{iban}", iban)
                            .with(csrf())
                            .with(jwt().authorities(new SimpleGrantedAuthority(ROLE_ADMIN))))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Failure: Closing non-existent account returns 404")
        void closeAccount_NotFound_Returns404() throws Exception {
            // Given
            String iban = "TR-NOT-FOUND";

            // When
            doThrow(new BankingServiceException("Account not found", HttpStatus.NOT_FOUND))
                    .when(accountService).closeAccount(iban);

            // Then
            mockMvc.perform(delete(BASE_URL + "/{iban}", iban)
                            .with(csrf())
                            .with(jwt().authorities(new SimpleGrantedAuthority(ROLE_ADMIN))))
                    .andExpect(status().isNotFound());
        }
    }
}