package com.batuhan.banking_service.repository;

import com.batuhan.banking_service.TestDataFactory;
import com.batuhan.banking_service.entity.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

/**
 * Base configuration for full-stack integration tests.
 * Spins up a real ApplicationContext with a randomized port and provides
 * pre-configured tools for API interaction, database manipulation, and security mocking.
 * Uses @Transactional to ensure database changes are rolled back after each test execution.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public abstract class BaseIntegrationTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected JdbcTemplate jdbcTemplate;

    // --- INFRASTRUCTURE MOCKS ---
    @MockitoBean
    protected JwtDecoder jwtDecoder;

    @MockitoBean
    private RabbitTemplate rabbitTemplate;

    @MockitoBean
    private AmqpAdmin amqpAdmin;

    // --- REPOSITORIES ---
    @Autowired
    protected AccountRepository accountRepository;

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected AccountLimitRepository accountLimitRepository;

    @Autowired
    protected TransactionRepository transactionRepository;

    // --- DATA SETUP HELPERS ---
    protected UserEntity saveTestUser() {
        return userRepository.save(TestDataFactory.createTestUser());
    }

    protected AccountEntity saveAccount(UserEntity user, String iban, String balance) {
        AccountEntity account = TestDataFactory.createTestAccount(user, iban);
        account.setBalance(new BigDecimal(balance));
        return accountRepository.save(account);
    }

    protected AccountEntity createAndSaveAccount(String iban, String balance) {
        UserEntity user = saveTestUser();
        return saveAccount(user, iban, balance);
    }

    // --- DATABASE MANIPULATION HELPERS ---
    protected void updateCreatedAt(String tableName, String description, LocalDateTime dateTime) {
        jdbcTemplate.update("UPDATE " + tableName + " SET created_at = ? WHERE description = ?",
                dateTime, description);
    }

    // --- REST API REQUEST HELPERS ---
    protected ResultActions performPost(String url, Object request) throws Exception {
        return mockMvc.perform(MockMvcRequestBuilders.post(url)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print());
    }

    protected ResultActions performGet(String url, Object... uriVars) throws Exception {
        return mockMvc.perform(MockMvcRequestBuilders.get(url, uriVars)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print());
    }

    // --- CLEANUP UTILS ---
    protected void clearDatabase() {
        transactionRepository.deleteAllInBatch();
        accountLimitRepository.deleteAllInBatch();
        accountRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
    }
}