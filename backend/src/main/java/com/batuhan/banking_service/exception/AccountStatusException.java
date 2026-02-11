package com.batuhan.banking_service.exception;

public class AccountStatusException extends BankingServiceException {

    public AccountStatusException(String message) {
        super(message);
    }
}