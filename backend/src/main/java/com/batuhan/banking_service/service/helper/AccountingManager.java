package com.batuhan.banking_service.service.helper;

import com.batuhan.banking_service.entity.AccountEntity;
import com.batuhan.banking_service.entity.AccountLimitEntity;
import com.batuhan.banking_service.repository.AccountLimitRepository;
import com.batuhan.banking_service.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

@Slf4j
@Component
@RequiredArgsConstructor
public class AccountingManager {

    private final AccountRepository accountRepository;
    private final AccountLimitRepository limitRepository;

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void processAccounting(AccountEntity sender, AccountEntity receiver, BigDecimal amount) {
        log.info("Executing balance update: [Sender: {}] -> [Receiver: {}] | Amount: {}",
                sender.getIban(), receiver.getIban(), amount);

        if (sender.getBalance().compareTo(amount) < 0) {
            log.error("Accounting failed: Insufficient balance for IBAN: {}", sender.getIban());
            throw new RuntimeException("Insufficient balance");
        }

        sender.setBalance(sender.getBalance().subtract(amount));
        receiver.setBalance(receiver.getBalance().add(amount));
        updateDailyLimitUsage(sender, amount);

        accountRepository.save(sender);
        accountRepository.save(receiver);
        log.info("Accounting process successfully completed for Transaction.");
    }

    private void updateDailyLimitUsage(AccountEntity sender, BigDecimal amount) {
        LocalDate today = LocalDate.now();

        AccountLimitEntity limit = limitRepository.findByAccountIdAndLimitDate(sender.getId(), today)
                .orElseGet(() -> {
                    log.debug("No limit record found for today ({}). Creating new record for account: {}",
                            today, sender.getIban());
                    return AccountLimitEntity.builder()
                            .account(sender)
                            .usedAmount(BigDecimal.ZERO)
                            .dailyLimit(sender.getDailyLimit())
                            .limitDate(today)
                            .active(true)
                            .build();
                });

        BigDecimal newUsedAmount = limit.getUsedAmount().add(amount);

        if (newUsedAmount.compareTo(limit.getDailyLimit()) > 0) {
            log.error("Accounting failed: Daily limit exceeded for IBAN: {}", sender.getIban());
            throw new RuntimeException("Daily limit exceeded");
        }

        limit.setUsedAmount(newUsedAmount);
        limit.setDailyLimit(sender.getDailyLimit());

        limitRepository.save(limit);
        log.debug("Daily limit updated: {} used today for IBAN: {}", limit.getUsedAmount(), sender.getIban());
    }
}