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
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final AccountMapper accountMapper;
    private final BankingBusinessValidator businessValidator;
    private final SecureRandom secureRandom = new SecureRandom();

    private static final String IBAN_ALREADY_EXISTS = "Could not generate a unique IBAN after 10 attempts";

    @Override
    @Transactional
    public AccountResponse createAccount(AccountCreateRequest request) {
        log.info("Starting account creation process for Customer: {}", request.customerNumber());
        UserEntity user = businessValidator.validateAndGetCustomer(request.customerNumber());
        businessValidator.validateOwnership(user);

        businessValidator.validateMaxAccountCount(user);
        AccountEntity account = prepareNewAccount(request, user);
        AccountEntity savedAccount = accountRepository.save(account);

        log.info("Successfully created account. IBAN: {}, Currency: {}",
                savedAccount.getIban(), savedAccount.getCurrency());
        return accountMapper.toResponse(savedAccount);
    }

    @Override
    @Transactional(readOnly = true)
    public AccountResponse getAccountByIban(String iban) {
        String cleanIban = iban.trim();
        log.info("Fetching account details for IBAN: [{}]", cleanIban);
        AccountEntity account = findAccountEntity(cleanIban);
        businessValidator.validateOwnership(account.getUser());
        return accountMapper.toResponse(account);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AccountResponse> getAccountsByCustomerNumber(String customerNumber) {
        log.info("Listing accounts for customer: {}", customerNumber);
        UserEntity user = businessValidator.validateAndGetCustomer(customerNumber);
        businessValidator.validateOwnership(user);
        return accountRepository.findByUserCustomerNumber(customerNumber).stream()
                .map(accountMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    @CacheEvict(value = "accounts", key = "#iban")
    public void closeAccount(String iban) {
        String cleanIban = iban.trim();
        log.warn("Initiating account closure for IBAN: {}", cleanIban);

        AccountEntity account = findAccountEntity(cleanIban);

        businessValidator.validateOwnership(account.getUser());
        validateAccountForClosure(account);

        account.setStatus(AccountStatus.CLOSED);
        account.setActive(false);

        accountRepository.save(account);
        log.info("Account {} successfully closed", cleanIban);
    }

    private AccountEntity findAccountEntity(String iban) {
        return accountRepository.findByIban(iban)
                .orElseThrow(() -> new BankingServiceException("Account not found with IBAN: " + iban, HttpStatus.NOT_FOUND));
    }

    private void validateAccountForClosure(AccountEntity account) {
        if (account.getBalance().compareTo(BigDecimal.ZERO) > 0) {
            log.error("Closure failed for IBAN: {}. Balance is not zero.", account.getIban());
            throw new BankingServiceException("Cannot close account with remaining balance: " + account.getBalance(), HttpStatus.BAD_REQUEST);
        }

        if (account.getStatus() == AccountStatus.CLOSED) {
            throw new BankingServiceException("Account is already closed.", HttpStatus.BAD_REQUEST);
        }
    }

    private AccountEntity prepareNewAccount(AccountCreateRequest request, UserEntity user) {
        AccountEntity account = accountMapper.toEntity(request);
        account.setUser(user);
        account.setIban(generateUniqueIban());
        account.setStatus(AccountStatus.ACTIVE);
        account.setActive(true);
        account.setBalance(BigDecimal.ZERO);

        if (account.getDailyLimit() == null || account.getDailyLimit().compareTo(BigDecimal.ZERO) == 0) {
            account.setDailyLimit(new BigDecimal("50000.00"));
        }

        return account;
    }

    private String generateUniqueIban() {
        String iban;
        int attempts = 0;
        do {
            iban = buildIbanString();
            attempts++;
            if (attempts > 10) {
                throw new BankingServiceException(IBAN_ALREADY_EXISTS, HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } while (accountRepository.existsByIban(iban));
        return iban;
    }

    private String buildIbanString() {
        String countryCode = "TR";
        String bankCode = "00062";

        StringBuilder accountPart = new StringBuilder();
        for (int i = 0; i < 17; i++) {
            accountPart.append(secureRandom.nextInt(10));
        }

        String forCheck = bankCode + accountPart + "292700";
        BigInteger checkNum = new BigInteger(forCheck);
        int checkDigit = 98 - checkNum.mod(BigInteger.valueOf(97)).intValue();

        return countryCode + String.format("%02d", checkDigit) + bankCode + accountPart;
    }
}