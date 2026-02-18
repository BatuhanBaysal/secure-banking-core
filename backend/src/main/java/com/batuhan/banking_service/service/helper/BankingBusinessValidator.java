package com.batuhan.banking_service.service.helper;

import com.batuhan.banking_service.entity.AccountEntity;
import com.batuhan.banking_service.entity.AccountLimitEntity;
import com.batuhan.banking_service.entity.UserEntity;
import com.batuhan.banking_service.entity.enums.AccountStatus;
import com.batuhan.banking_service.exception.BankingServiceException;
import com.batuhan.banking_service.repository.AccountLimitRepository;
import com.batuhan.banking_service.repository.AccountRepository;
import com.batuhan.banking_service.repository.TransactionRepository;
import com.batuhan.banking_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
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
    private  final TransactionRepository transactionRepository;

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

    @Transactional(readOnly = true)
    public boolean isOwner(String customerNumber) {
        if (isAdmin()) return true;

        String currentUserEmail = getAuthenticatedUserEmail();
        return userRepository.findByCustomerNumber(customerNumber)
                .map(user -> user.getEmail().equalsIgnoreCase(currentUserEmail))
                .orElse(false);
    }

    @Transactional(readOnly = true)
    public boolean isAccountOwner(String iban) {
        if (isAdmin()) return true;

        String currentUserEmail = getAuthenticatedUserEmail();
        return accountRepository.findByIban(iban.trim())
                .map(account -> account.getUser().getEmail().equalsIgnoreCase(currentUserEmail))
                .orElse(false);
    }

    @Transactional(readOnly = true)
    public boolean isTransactionOwner(Long transactionId) {
        if (isAdmin()) return true;

        String currentUserEmail = getAuthenticatedUserEmail();
        return transactionRepository.findById(transactionId)
                .map(transaction ->
                        transaction.getSenderAccount().getUser().getEmail().equalsIgnoreCase(currentUserEmail) ||
                                transaction.getReceiverAccount().getUser().getEmail().equalsIgnoreCase(currentUserEmail))
                .orElse(false);
    }

    public void validateOwnership(UserEntity targetUser) {
        if (isAdmin()) return;

        String currentUserEmail = getAuthenticatedUserEmail();
        log.info("[SECURITY CHECK] Current User: {}, Target User: {}", currentUserEmail, targetUser.getEmail());

        if (targetUser == null || currentUserEmail == null || !targetUser.getEmail().equalsIgnoreCase(currentUserEmail)) {
            log.error("SECURITY ALERT: Access Denied for User {}", currentUserEmail);
            throw new BankingServiceException("Access Denied: You are not authorized for this operation!", HttpStatus.FORBIDDEN);
        }
    }

    @Transactional(readOnly = true)
    public void validateTransferRules(AccountEntity sender, AccountEntity receiver, BigDecimal amount) {
        validateOwnership(sender.getUser());

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BankingServiceException("Invalid amount! Amount must be greater than zero.", HttpStatus.BAD_REQUEST);
        }
        if (sender.getIban().equalsIgnoreCase(receiver.getIban())) {
            throw new BankingServiceException("Self-transfer within the same IBAN is not allowed.", HttpStatus.BAD_REQUEST);
        }

        validateAccountStatus(sender, "Sender");
        validateAccountStatus(receiver, "Receiver");

        if (!receiver.getUser().isActive()) {
            throw new BankingServiceException("Receiver user is inactive or closed.", HttpStatus.BAD_REQUEST);
        }
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

    @Transactional(readOnly = true)
    public void validateDailyLimit(AccountEntity sender, BigDecimal amount) {
        BigDecimal usedToday = limitRepository.findByAccountIdAndLimitDate(sender.getId(), LocalDate.now())
                .map(AccountLimitEntity::getUsedAmount)
                .orElse(BigDecimal.ZERO);

        if (usedToday.add(amount).compareTo(sender.getDailyLimit()) > 0) {
            BigDecimal remaining = sender.getDailyLimit().subtract(usedToday);
            throw new BankingServiceException("Daily transfer limit exceeded! Remaining limit: " + remaining, HttpStatus.BAD_REQUEST);
        }
    }

    public boolean isAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return false;

        return auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(role -> role.equalsIgnoreCase("ROLE_ADMIN") ||
                        role.equalsIgnoreCase("ADMIN") ||
                        role.equalsIgnoreCase("SCOPE_ADMIN"));
    }

    public String getAuthenticatedUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof Jwt jwt) {
            String email = jwt.getClaimAsString("email");
            return (email != null) ? email : jwt.getClaimAsString("preferred_username");
        }
        return authentication.getName();
    }
}