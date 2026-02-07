package com.batuhan.banking_service.service.impl;

import com.batuhan.banking_service.dto.request.AccountCreateRequest;
import com.batuhan.banking_service.dto.response.AccountResponse;
import com.batuhan.banking_service.entity.AccountEntity;
import com.batuhan.banking_service.entity.UserEntity;
import com.batuhan.banking_service.entity.enums.AccountStatus;
import com.batuhan.banking_service.exception.BankingServiceException;
import com.batuhan.banking_service.mapper.AccountMapper;
import com.batuhan.banking_service.repository.AccountRepository;
import com.batuhan.banking_service.service.AccountService;
import com.batuhan.banking_service.service.helper.BankingBusinessValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final AccountMapper accountMapper;
    private final BankingBusinessValidator businessValidator;
    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    @Transactional
    public AccountResponse createAccount(AccountCreateRequest request) {
        log.info("Starting account creation for Customer: {}", request.getCustomerNumber());
        UserEntity user = businessValidator.validateAndGetCustomer(request.getCustomerNumber());
        businessValidator.validateOwnership(user);
        businessValidator.validateMaxAccountCount(user);

        AccountEntity account = prepareNewAccount(request, user);
        AccountEntity savedAccount = accountRepository.save(account);

        log.info("Successfully created account. IBAN: {}", savedAccount.getIban());
        return accountMapper.toResponse(savedAccount);
    }

    @Override
    @Transactional(readOnly = true)
    public AccountResponse getAccountByIban(String iban) {
        AccountEntity account = accountRepository.findByIban(iban)
                .orElseThrow(() -> new BankingServiceException("Account not found: " + iban, HttpStatus.NOT_FOUND));

        businessValidator.validateOwnership(account.getUser());
        return accountMapper.toResponse(account);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AccountResponse> getAccountsByCustomerNumber(String customerNumber) {
        UserEntity user = businessValidator.validateAndGetCustomer(customerNumber);
        businessValidator.validateOwnership(user);
        return accountRepository.findByUserCustomerNumber(customerNumber).stream()
                .map(accountMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void closeAccount(String iban) {
        log.warn("Initiating account closure for IBAN: {}", iban);
        AccountEntity account = accountRepository.findByIban(iban)
                .orElseThrow(() -> new BankingServiceException("Account not found with IBAN: " + iban, HttpStatus.NOT_FOUND));

        businessValidator.validateOwnership(account.getUser());
        if (account.getBalance().compareTo(BigDecimal.ZERO) > 0) {
            throw new BankingServiceException("Cannot close account with remaining balance. Please withdraw funds first.", HttpStatus.BAD_REQUEST);
        }

        account.setStatus(AccountStatus.CLOSED);
        account.setActive(false);
        accountRepository.save(account);

        log.info("Account successfully closed: {}", iban);
    }

    private AccountEntity prepareNewAccount(AccountCreateRequest request, UserEntity user) {
        AccountEntity account = accountMapper.toEntity(request);
        account.setUser(user);
        account.setIban(generateUniqueIban());

        BigDecimal initialBalance = request.getInitialBalance() != null ? request.getInitialBalance() : BigDecimal.ZERO;
        account.setBalance(initialBalance);
        account.setStatus(AccountStatus.ACTIVE);
        account.setActive(true);

        if (account.getDailyLimit() == null) {
            account.setDailyLimit(new BigDecimal("5000.00"));
        }

        return account;
    }

    private String generateUniqueIban() {
        String iban;
        do {
            String countryCode = "TR";
            String bankCode = "00062";

            StringBuilder accountPart = new StringBuilder();
            for (int i = 0; i < 17; i++) {
                accountPart.append(secureRandom.nextInt(10));
            }

            String forCheck = bankCode + accountPart + "292700";
            java.math.BigInteger checkNum = new java.math.BigInteger(forCheck);
            int checkDigit = 98 - checkNum.mod(new java.math.BigInteger("97")).intValue();

            iban = countryCode + String.format("%02d", checkDigit) + bankCode + accountPart;
        } while (accountRepository.existsByIban(iban));

        return iban;
    }
}