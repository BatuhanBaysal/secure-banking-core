package com.batuhan.banking_service.service.impl;

import com.batuhan.banking_service.dto.common.TransactionCategoryDTO;
import com.batuhan.banking_service.dto.common.TransactionSummaryDTO;
import com.batuhan.banking_service.dto.common.WeeklyTrendDTO;
import com.batuhan.banking_service.dto.request.TransactionRequest;
import com.batuhan.banking_service.dto.response.TransactionResponse;
import com.batuhan.banking_service.entity.AccountEntity;
import com.batuhan.banking_service.entity.AccountLimitEntity;
import com.batuhan.banking_service.entity.TransactionEntity;
import com.batuhan.banking_service.entity.enums.TransactionStatus;
import com.batuhan.banking_service.entity.enums.TransactionType;
import com.batuhan.banking_service.exception.BankingServiceException;
import com.batuhan.banking_service.mapper.TransactionMapper;
import com.batuhan.banking_service.repository.AccountLimitRepository;
import com.batuhan.banking_service.repository.AccountRepository;
import com.batuhan.banking_service.repository.TransactionRepository;
import com.batuhan.banking_service.repository.specification.TransactionSpecifications;
import com.batuhan.banking_service.service.AuditService;
import com.batuhan.banking_service.service.EmailService;
import com.batuhan.banking_service.service.PdfService;
import com.batuhan.banking_service.service.TransactionService;
import com.batuhan.banking_service.service.helper.AccountingManager;
import com.batuhan.banking_service.service.helper.BankingBusinessValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final AccountLimitRepository limitRepository;
    private final TransactionMapper transactionMapper;
    private final PdfService pdfService;
    private final EmailService emailService;
    private final AuditService auditService;
    private final BankingBusinessValidator businessValidator;
    private final AccountingManager accountingManager;
    private final TransactionAnalysisServiceImpl analysisService;

    @Override
    @Transactional
    public TransactionResponse transferMoney(TransactionRequest request) {
        log.info("Transfer: {} -> {} Amount: {}", request.getSenderIban(), request.getReceiverIban(), request.getAmount());
        validateSelfTransfer(request);

        List<AccountEntity> accounts = lockAccountsAlphabetically(request.getSenderIban(), request.getReceiverIban());
        AccountEntity sender = findAccountInList(accounts, request.getSenderIban());
        AccountEntity receiver = findAccountInList(accounts, request.getReceiverIban());

        businessValidator.validateTransferRules(sender, receiver, request.getAmount());
        accountingManager.processAccounting(sender, receiver, request.getAmount());

        TransactionEntity transaction = saveTransactionRecord(request, sender, receiver);
        finalizeTransaction(sender, receiver, transaction);
        return transactionMapper.toResponse(transaction);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TransactionResponse> getTransactionHistory(String iban, Pageable pageable) {
        AccountEntity account = getAccountByIban(iban);
        businessValidator.validateOwnership(account.getUser());
        return transactionRepository.findAllByIban(iban, pageable)
                .map(transactionMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransactionResponse> getAllTransactionsByIban(String iban) {
        AccountEntity account = getAccountByIban(iban);
        businessValidator.validateOwnership(account.getUser());
        return transactionRepository.findAll(TransactionSpecifications.hasIban(iban))
                .stream()
                .map(transactionMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public TransactionSummaryDTO getDashboardSummary(String iban) {
        AccountEntity account = getAccountByIban(iban);
        businessValidator.validateOwnership(account.getUser());
        return transactionRepository.getTransactionSummary(iban);
    }

    @Override
    @Transactional(readOnly = true)
    public List<WeeklyTrendDTO> getWeeklyTrend(String iban) {
        AccountEntity account = getAccountByIban(iban);
        businessValidator.validateOwnership(account.getUser());
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        return transactionRepository.getWeeklySpendingTrend(iban, sevenDaysAgo);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransactionCategoryDTO> getCategoryAnalysis(String iban) {
        AccountEntity account = getAccountByIban(iban);
        businessValidator.validateOwnership(account.getUser());
        return analysisService.calculateCategoryAnalysis(iban);
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] generateTransactionReceipt(Long id) {
        TransactionEntity transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new BankingServiceException("Transaction not found with ID: " + id, HttpStatus.NOT_FOUND));

        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        boolean isAuthorized = transaction.getSenderAccount().getUser().getEmail().equals(currentUserEmail) ||
                transaction.getReceiverAccount().getUser().getEmail().equals(currentUserEmail);

        if (!isAuthorized) {
            log.error("Unauthorized receipt download attempt by user: {} for transaction ID: {}", currentUserEmail, id);
            throw new BankingServiceException("Access Denied: You are not authorized to download this receipt.", HttpStatus.FORBIDDEN);
        }

        try {
            return pdfService.generateTransactionReceipt(transaction).readAllBytes();
        } catch (Exception e) {
            log.error("PDF Receipt generation failed for transaction {}: {}", id, e.getMessage());
            throw new BankingServiceException("Could not generate receipt file. Please try again later.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TransactionResponse> filterTransactions(
            String iban, BigDecimal minAmount, BigDecimal maxAmount,
            LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {

        AccountEntity account = getAccountByIban(iban);
        businessValidator.validateOwnership(account.getUser());
        Specification<TransactionEntity> spec = Specification.where(TransactionSpecifications.hasIban(iban));

        if (minAmount != null || maxAmount != null) {
            spec = spec.and(TransactionSpecifications.amountBetween(minAmount, maxAmount));
        }
        if (startDate != null || endDate != null) {
            spec = spec.and(TransactionSpecifications.dateBetween(startDate, endDate));
        }

        return transactionRepository.findAll(spec, pageable)
                .map(transactionMapper::toResponse);
    }

    private AccountEntity getAccountByIban(String iban) {
        return accountRepository.findByIban(iban)
                .orElseThrow(() -> new BankingServiceException("Account not found with IBAN: " + iban, HttpStatus.NOT_FOUND));
    }

    private void executeAccountingEntries(AccountEntity sender, AccountEntity receiver, BigDecimal amount) {
        sender.setBalance(sender.getBalance().subtract(amount));
        receiver.setBalance(receiver.getBalance().add(amount));

        updateDailyLimit(sender, amount);

        accountRepository.save(sender);
        accountRepository.save(receiver);
    }

    private void updateDailyLimit(AccountEntity sender, BigDecimal amount) {
        AccountLimitEntity limit = limitRepository.findByAccountIdAndLimitDate(sender.getId(), LocalDate.now())
                .orElseGet(() -> AccountLimitEntity.builder()
                        .account(sender)
                        .usedAmount(BigDecimal.ZERO)
                        .dailyLimit(sender.getDailyLimit())
                        .limitDate(LocalDate.now())
                        .isActive(true)
                        .build());

        limit.setUsedAmount(limit.getUsedAmount().add(amount));
        limitRepository.save(limit);
    }

    private TransactionEntity saveTransactionRecord(TransactionRequest request, AccountEntity sender, AccountEntity receiver) {
        String referenceNumber = "TX-" + LocalDate.now().toString().replace("-", "") + "-"
                + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        return transactionRepository.save(TransactionEntity.builder()
                .referenceNumber(referenceNumber)
                .senderAccount(sender)
                .receiverAccount(receiver)
                .amount(request.getAmount())
                .transactionType(TransactionType.TRANSFER)
                .status(TransactionStatus.COMPLETED)
                .description(request.getDescription())
                .build());
    }

    private void finalizeTransaction(AccountEntity sender, AccountEntity receiver, TransactionEntity transaction) {
        auditService.log("MONEY_TRANSFER", sender.getUser().getEmail(),
                String.format("Transfer of %s %s to %s. Reference: %s",
                        transaction.getAmount(), sender.getCurrency(),
                        receiver.getIban(), transaction.getReferenceNumber()));

        try {
            emailService.sendTransferEmail(sender.getUser().getEmail(), sender.getUser().getFirstName(),
                    transaction.getAmount(), receiver.getIban(), transaction.getReferenceNumber());
        } catch (Exception e) {
            log.warn("Non-critical notification failure (Email): {}", e.getMessage());
        }
    }

    private List<AccountEntity> lockAccountsAlphabetically(String iban1, String iban2) {
        return java.util.stream.Stream.of(iban1, iban2)
                .sorted()
                .map(iban -> accountRepository.findByIbanWithLock(iban)
                        .orElseThrow(() -> new BankingServiceException("Target account for lock not found: " + iban, HttpStatus.NOT_FOUND)))
                .toList();
    }

    private void validateSelfTransfer(TransactionRequest request) {
        if (request.getSenderIban().equals(request.getReceiverIban())) {
            throw new BankingServiceException("Cannot transfer money to the same account", HttpStatus.BAD_REQUEST);
        }
    }

    private AccountEntity findAccountInList(List<AccountEntity> accounts, String iban) {
        return accounts.stream()
                .filter(a -> a.getIban().equals(iban))
                .findFirst()
                .orElseThrow(() -> new BankingServiceException("Account not found in locked list: " + iban, HttpStatus.NOT_FOUND));
    }
}