package com.batuhan.banking_service.exception;

public class InsufficientBalanceException extends BankingServiceException {

    public InsufficientBalanceException(String message) {
        super(message);
    }
}