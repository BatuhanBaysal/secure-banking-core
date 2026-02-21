package com.batuhan.banking_service.service;

import com.batuhan.banking_service.TestDataFactory;
import com.batuhan.banking_service.dto.response.TransactionResponse;
import com.batuhan.banking_service.entity.AccountEntity;
import com.batuhan.banking_service.entity.enums.TransactionStatus;
import com.batuhan.banking_service.entity.enums.TransactionType;
import com.batuhan.banking_service.exception.BankingServiceException;
import com.batuhan.banking_service.mapper.AccountMapper;
import com.batuhan.banking_service.mapper.TransactionMapper;
import com.batuhan.banking_service.mapper.UserMapper;
import com.batuhan.banking_service.repository.AccountLimitRepository;
import com.batuhan.banking_service.repository.AccountRepository;
import com.batuhan.banking_service.repository.TransactionRepository;
import com.batuhan.banking_service.repository.UserRepository;
import com.batuhan.banking_service.service.helper.AccountingManager;
import com.batuhan.banking_service.service.helper.BankingBusinessValidator;
import com.batuhan.banking_service.service.impl.TransactionAnalysisServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Base abstract class for Service layer unit tests.
 * Utilizes Mockito to isolate business logic from database and external system dependencies.
 * Provides shared utility methods for simulating security contexts, transaction flows,
 * and common business exception scenarios.
 */
@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
public abstract class BaseServiceTest {

    // --- (REPOSITORIES) ---
    @Mock protected AccountRepository accountRepository;
    @Mock protected TransactionRepository transactionRepository;
    @Mock protected UserRepository userRepository;
    @Mock protected AccountLimitRepository limitRepository;

    // --- (MAPPERS) ---
    @Mock protected AccountMapper accountMapper;
    @Mock protected TransactionMapper transactionMapper;
    @Mock protected UserMapper userMapper;

    // --- (BUSINESS HELPERS & EXTERNAL SERVICES) ---
    @Mock protected PasswordEncoder passwordEncoder;
    @Mock protected BankingBusinessValidator bankingBusinessValidator;
    @Mock protected AccountingManager accountingManager;
    @Mock protected TransactionAnalysisServiceImpl analysisService;
    @Mock protected EmailService emailService;
    @Mock protected AuditService auditService;
    @Mock protected PdfService pdfService;
    @Mock protected RabbitMQProducer rabbitMQProducer;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    protected void mockCurrentUser(String email) {
        Authentication auth = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);

        lenient().when(auth.getName()).thenReturn(email);
        lenient().when(auth.isAuthenticated()).thenReturn(true);
        lenient().when(securityContext.getAuthentication()).thenReturn(auth);

        SecurityContextHolder.setContext(securityContext);
    }

    protected void mockAccountLookup(AccountEntity account) {
        lenient().when(accountRepository.findByIban(account.getIban()))
                .thenReturn(Optional.of(account));
        lenient().when(accountRepository.findByIbanWithLock(account.getIban()))
                .thenReturn(Optional.of(account));
    }

    protected TransactionResponse createMockTransactionResponse(String refNo, BigDecimal amount, String sIban, String rIban) {
        return new TransactionResponse(
                UUID.randomUUID(),
                sIban, "Sender Name",
                rIban, "Receiver Name",
                amount,
                TransactionType.TRANSFER,
                TransactionStatus.COMPLETED,
                "Transfer success",
                LocalDateTime.now(),
                refNo
        );
    }

    protected void mockBusinessException(String message, HttpStatus status) {
        BankingServiceException exception = new BankingServiceException(message, status);
        lenient().doThrow(exception).when(bankingBusinessValidator).validateTransferRules(any(), any(), any());
        lenient().doThrow(exception).when(bankingBusinessValidator).validateOwnership(any());
        lenient().doThrow(exception).when(bankingBusinessValidator).validateAccountStatus(any(AccountEntity.class), anyString());
    }

    protected <T> void mockEmptyPage() {
        lenient().when(transactionRepository.findAllByIban(anyString(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(Collections.emptyList()));
    }

    protected void mockNotFoundScenarios() {
        lenient().when(accountRepository.findByIban(anyString())).thenReturn(Optional.empty());
        lenient().when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
    }

    protected void mockAccountPairLookup(String senderIban, String receiverIban) {
        var sender = TestDataFactory.createTestAccount(TestDataFactory.createTestUser(), senderIban);
        var receiver = TestDataFactory.createTestAccount(TestDataFactory.createTestUser(), receiverIban);

        lenient().when(accountRepository.findByIbanWithLock(senderIban)).thenReturn(Optional.of(sender));
        lenient().when(accountRepository.findByIbanWithLock(receiverIban)).thenReturn(Optional.of(receiver));
        lenient().when(accountRepository.findByIban(senderIban)).thenReturn(Optional.of(sender));
        lenient().when(accountRepository.findByIban(receiverIban)).thenReturn(Optional.of(receiver));
    }
}