package com.batuhan.banking_service.exception;

public class EmailAlreadyExistsException extends BankingServiceException {

    public EmailAlreadyExistsException(String message) {
        super(message);
    }
}