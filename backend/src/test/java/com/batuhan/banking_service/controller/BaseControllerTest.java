package com.batuhan.banking_service.controller;

import com.batuhan.banking_service.service.*;
import com.batuhan.banking_service.service.helper.BankingBusinessValidator;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.bulkhead.BulkheadRegistry;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Base abstract class for Web Layer (Controller) unit tests.
 * Focuses on testing REST endpoints using MockMvc without starting a full HTTP server.
 * All service dependencies and security filters are mocked to isolate the controller logic,
 * ensuring tests are fast and focused on request/response mapping, validation, and security constraints.
 */
@AutoConfigureMockMvc(addFilters = true)
@ActiveProfiles("test")
public abstract class BaseControllerTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    // --- SECURITY & INFRASTRUCTURE MOCKS ---
    @MockitoBean
    protected JwtDecoder jwtDecoder;

    @MockitoBean(name = "bankingBusinessValidator")
    protected BankingBusinessValidator bankingBusinessValidator;

    // --- CORE BUSINESS SERVICE MOCKS ---
    @MockitoBean
    protected AccountService accountService;

    @MockitoBean
    protected TransactionService transactionService;

    @MockitoBean
    protected UserService userService;

    @MockitoBean
    protected ExcelService excelService;

    // --- RESILIENCE4J MOCKS ---
    @MockitoBean
    protected RateLimiterRegistry rateLimiterRegistry;

    @MockitoBean
    protected BulkheadRegistry bulkheadRegistry;
}