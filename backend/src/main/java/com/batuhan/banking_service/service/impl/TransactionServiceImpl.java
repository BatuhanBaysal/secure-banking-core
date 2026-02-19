package com.batuhan.banking_service.service.impl;

import com.batuhan.banking_service.dto.common.TransactionCategoryDTO;
import com.batuhan.banking_service.dto.common.TransactionSummaryDTO;
import com.batuhan.banking_service.dto.common.WeeklyTrendDTO;
import com.batuhan.banking_service.dto.event.TransferEvent;
import com.batuhan.banking_service.dto.request.TransactionRequest;
import com.batuhan.banking_service.dto.response.TransactionResponse;
import com.batuhan.banking_service.entity.AccountEntity;
import com.batuhan.banking_service.entity.TransactionEntity;
import com.batuhan.banking_service.entity.enums.TransactionStatus;
import com.batuhan.banking_service.entity.enums.TransactionType;
import com.batuhan.banking_service.exception.BankingServiceException;
import com.batuhan.banking_service.mapper.TransactionMapper;
import com.batuhan.banking_service.repository.AccountRepository;
import com.batuhan.banking_service.repository.TransactionRepository;
import com.batuhan.banking_service.repository.specification.TransactionSpecifications;
import com.batuhan.banking_service.service.*;
import com.batuhan.banking_service.service.helper.AccountingManager;
import com.batuhan.banking_service.service.helper.BankingBusinessValidator;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final TransactionMapper transactionMapper;
    private final PdfService pdfService;
    private final RabbitMQProducer rabbitMQProducer;
    private final AuditService auditService;
    private final BankingBusinessValidator businessValidator;
    private final AccountingManager accountingManager;
    private final TransactionAnalysisServiceImpl analysisService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "accounts", allEntries = true)
    public TransactionResponse transferMoney(TransactionRequest request) {
        log.info("Processing transfer: {} to {} amount: {}", request.senderIban(), request.receiverIban(), request.amount());
        validateSelfTransfer(request);

        List<AccountEntity> lockedAccounts = lockAccountsAlphabetically(request.senderIban(), request.receiverIban());

        AccountEntity sender = findAccountInList(lockedAccounts, request.senderIban());
        AccountEntity receiver = findAccountInList(lockedAccounts, request.receiverIban());

        businessValidator.validateTransferRules(sender, receiver, request.amount());
        accountingManager.processAccounting(sender, receiver, request.amount());

        TransactionEntity transaction = saveTransactionRecord(request, sender, receiver);
        finalizeTransaction(sender, receiver, transaction);
        return transactionMapper.toResponse(transaction);
    }

    private void finalizeTransaction(AccountEntity sender, AccountEntity receiver, TransactionEntity transaction) {
        auditService.log("MONEY_TRANSFER", getAuthenticatedUserEmail(), "Reference: " + transaction.getReferenceNumber());

        try {
            sendNotification(sender, receiver, transaction);
        } catch (Exception e) {
            log.error("Notification could not be sent for Reference: {}. Error: {}",
                    transaction.getReferenceNumber(), e.getMessage(), e);
        }
    }

    private void sendNotification(AccountEntity sender, AccountEntity receiver, TransactionEntity transaction) {
        TransferEvent event = new TransferEvent(
                sender.getUser().getEmail(),
                sender.getUser().getFirstName(),
                transaction.getAmount(),
                receiver.getIban(),
                transaction.getReferenceNumber()
        );
        rabbitMQProducer.sendToQueue(event);
    }

    private TransactionEntity saveTransactionRecord(TransactionRequest request, AccountEntity sender, AccountEntity receiver) {
        String ref = "TX-%d-%s".formatted(
                System.currentTimeMillis(),
                UUID.randomUUID().toString().substring(0, 5).toUpperCase()
        );

        return transactionRepository.save(TransactionEntity.builder()
                .referenceNumber(ref)
                .senderAccount(sender)
                .receiverAccount(receiver)
                .amount(request.amount())
                .transactionType(TransactionType.TRANSFER)
                .status(TransactionStatus.COMPLETED)
                .description(request.description())
                .build());
    }

    @Override
    @Transactional(readOnly = true)
    @CircuitBreaker(name = "receiptService", fallbackMethod = "receiptFallback")
    @Retry(name = "receiptService")
    public byte[] generateTransactionReceipt(Long id) {
        TransactionEntity transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new BankingServiceException("Transaction not found", HttpStatus.NOT_FOUND));

        authorizeReceiptAccess(transaction);

        try {
            return pdfService.generateTransactionReceipt(transaction).readAllBytes();
        } catch (Exception e) {
            log.error("Receipt generation error for TX {}: {}", id, e.getMessage(), e);
            throw new BankingServiceException("Could not generate PDF receipt", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public byte[] receiptFallback(Long id, Exception e) {
        log.error("Receipt fallback triggered for TX: {}. Reason: {}", id, e.getMessage());
        throw new BankingServiceException("PDF service is busy, please try again later.", HttpStatus.SERVICE_UNAVAILABLE);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TransactionResponse> getTransactionHistory(String iban, Pageable pageable) {
        validateAccountAccess(iban);
        return transactionRepository.findAllByIban(iban, pageable).map(transactionMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransactionResponse> getAllTransactionsByIban(String iban) {
        validateAccountAccess(iban);
        return transactionRepository.findAll(TransactionSpecifications.hasIban(iban))
                .stream()
                .map(transactionMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public TransactionSummaryDTO getDashboardSummary(String iban) {
        validateAccountAccess(iban);
        return transactionRepository.getTransactionSummary(iban);
    }

    @Override
    @Transactional(readOnly = true)
    public List<WeeklyTrendDTO> getWeeklyTrend(String iban) {
        validateAccountAccess(iban);
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        return transactionRepository.getWeeklySpendingTrend(iban, sevenDaysAgo);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransactionCategoryDTO> getCategoryAnalysis(String iban) {
        validateAccountAccess(iban);
        return analysisService.calculateCategoryAnalysis(iban);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TransactionResponse> filterTransactions(
            String iban, BigDecimal minAmount, BigDecimal maxAmount,
            LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {

        validateAccountAccess(iban);
        Specification<TransactionEntity> spec = Specification.where(TransactionSpecifications.hasIban(iban));

        if (minAmount != null || maxAmount != null) {
            spec = spec.and(TransactionSpecifications.amountBetween(minAmount, maxAmount));
        }
        if (startDate != null || endDate != null) {
            spec = spec.and(TransactionSpecifications.dateBetween(startDate, endDate));
        }

        return transactionRepository.findAll(spec, pageable).map(transactionMapper::toResponse);
    }

    private void validateAccountAccess(String iban) {
        if (!businessValidator.isAccountOwner(iban)) {
            throw new BankingServiceException("Access Denied for IBAN: " + iban, HttpStatus.FORBIDDEN);
        }
    }

    private void authorizeReceiptAccess(TransactionEntity transaction) {
        if (!businessValidator.isTransactionOwner(transaction.getId())) {
            throw new BankingServiceException("You are not authorized to view this receipt", HttpStatus.FORBIDDEN);
        }
    }

    private List<AccountEntity> lockAccountsAlphabetically(String iban1, String iban2) {
        return java.util.stream.Stream.of(iban1, iban2)
                .sorted()
                .map(iban -> accountRepository.findByIbanWithLock(iban)
                        .orElseThrow(() -> new BankingServiceException("Account not found for locking: " + iban, HttpStatus.NOT_FOUND)))
                .toList();
    }

    private AccountEntity findAccountInList(List<AccountEntity> accounts, String iban) {
        return accounts.stream()
                .filter(a -> a.getIban().equalsIgnoreCase(iban.trim()))
                .findFirst()
                .orElseThrow(() -> new BankingServiceException("Internal error: Account lost during locking", HttpStatus.INTERNAL_SERVER_ERROR));
    }

    private void validateSelfTransfer(TransactionRequest request) {
        if (request.senderIban().trim().equalsIgnoreCase(request.receiverIban().trim())) {
            throw new BankingServiceException("Sender and receiver accounts cannot be the same", HttpStatus.BAD_REQUEST);
        }
    }

    private String getAuthenticatedUserEmail() {
        return businessValidator.getAuthenticatedUserEmail();
    }
}