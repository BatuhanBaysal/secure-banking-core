package com.batuhan.banking_service.service.helper;

import com.batuhan.banking_service.entity.AccountEntity;
import com.batuhan.banking_service.entity.AccountLimitEntity;
import com.batuhan.banking_service.repository.AccountLimitRepository;
import com.batuhan.banking_service.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class AccountingManager {

    private final AccountRepository accountRepository;
    private final AccountLimitRepository limitRepository;

    @Transactional(propagation = Propagation.MANDATORY)
    public void processAccounting(AccountEntity sender, AccountEntity receiver, BigDecimal amount) {
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
}