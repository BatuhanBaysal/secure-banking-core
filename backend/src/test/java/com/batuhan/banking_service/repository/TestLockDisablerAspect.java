package com.batuhan.banking_service.repository;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Performance & Stability Utility for Integration Tests.
 * This Aspect intercepts calls to pessimistic locking methods (like findByIbanWithLock)
 * and redirects them to standard find methods.
 * * Purpose: Prevents database deadlocks and timeout issues caused by H2's limited support
 * for row-level locking during high-concurrency test scenarios.
 */
@Aspect
@Configuration
@Profile("test")
public class TestLockDisablerAspect {

    @Around("execution(* com.batuhan.banking_service.repository.AccountRepository.findByIbanWithLock(..))")
    public Object disableLock(ProceedingJoinPoint joinPoint) throws Throwable {
        Object[] args = joinPoint.getArgs();
        String iban = (String) args[0];

        AccountRepository repository = (AccountRepository) joinPoint.getTarget();
        return repository.findByIban(iban);
    }
}