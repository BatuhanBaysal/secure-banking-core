package com.batuhan.banking_service.service.helper;

import com.batuhan.banking_service.entity.AccountEntity;
import com.batuhan.banking_service.entity.AccountLimitEntity;
import com.batuhan.banking_service.entity.UserEntity;
import com.batuhan.banking_service.entity.enums.AccountStatus;
import com.batuhan.banking_service.exception.BankingServiceException;
import com.batuhan.banking_service.repository.AccountLimitRepository;
import com.batuhan.banking_service.repository.AccountRepository;
import com.batuhan.banking_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

@Slf4j
@Component
@RequiredArgsConstructor
public class BankingBusinessValidator {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final AccountLimitRepository limitRepository;

    public UserEntity validateAndGetCustomer(String customerNumber) {
        return userRepository.findByCustomerNumber(customerNumber)
                .orElseThrow(() -> new BankingServiceException("Customer not found with number: " + customerNumber, HttpStatus.NOT_FOUND));
    }

    public void validateMaxAccountCount(UserEntity user) {
        long activeAccountCount = user.getAccounts().stream()
                .filter(acc -> acc.getStatus() == AccountStatus.ACTIVE)
                .count();

        if (activeAccountCount >= 10) {
            log.warn("Account limit reached for customer: {}", user.getCustomerNumber());
            throw new BankingServiceException("A user cannot have more than 10 active accounts.", HttpStatus.BAD_REQUEST);
        }
    }

    public void validateOwnership(UserEntity targetUser) {
        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        if (!targetUser.getEmail().equals(currentUserEmail)) {
            log.error("SECURITY ALERT: User {} attempted unauthorized access", currentUserEmail);
            throw new BankingServiceException("Access Denied: You are not authorized for this operation!", HttpStatus.FORBIDDEN);
        }
    }

    @Transactional(readOnly = true)
    public boolean isOwner(String customerNumber) {
        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByCustomerNumber(customerNumber)
                .map(user -> user.getEmail().equals(currentUserEmail))
                .orElse(false);
    }

    @Transactional(readOnly = true)
    public boolean isAccountOwner(String iban) {
        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        return accountRepository.findByIban(iban)
                .map(account -> account.getUser().getEmail().equals(currentUserEmail))
                .orElse(false);
    }

    public void validateTransferRules(AccountEntity sender, AccountEntity receiver, BigDecimal amount) {
        validateOwnership(sender.getUser());

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BankingServiceException("Invalid amount!", HttpStatus.BAD_REQUEST);
        }

        if (sender.getIban().equals(receiver.getIban())) {
            throw new BankingServiceException("Self-transfer within the same IBAN is not allowed.", HttpStatus.BAD_REQUEST);
        }

        validateAccountStatus(sender, "Sender");
        validateAccountStatus(receiver, "Receiver");

        if (sender.getCurrency() != receiver.getCurrency()) {
            throw new BankingServiceException("Cross-currency transfers are not yet supported.", HttpStatus.BAD_REQUEST);
        }

        if (sender.getBalance().compareTo(amount) < 0) {
            throw new BankingServiceException("Insufficient funds in sender account!", HttpStatus.BAD_REQUEST);
        }

        validateDailyLimit(sender, amount);
    }

    public void validateAccountStatus(AccountEntity account, String label) {
        if (account.getStatus() != AccountStatus.ACTIVE || !account.isActive()) {
            throw new BankingServiceException(label + " account is not active or is closed.", HttpStatus.BAD_REQUEST);
        }
    }

    public void validateDailyLimit(AccountEntity sender, BigDecimal amount) {
        BigDecimal usedToday = limitRepository.findByAccountIdAndLimitDate(sender.getId(), LocalDate.now())
                .map(AccountLimitEntity::getUsedAmount)
                .orElse(BigDecimal.ZERO);

        if (usedToday.add(amount).compareTo(sender.getDailyLimit()) > 0) {
            BigDecimal remaining = sender.getDailyLimit().subtract(usedToday);
            throw new BankingServiceException("Daily transfer limit exceeded! Remaining: " + remaining, HttpStatus.BAD_REQUEST);
        }
    }
}