package com.batuhan.banking_service.exception;

public class IneligibleAgeException extends BankingServiceException {

    public IneligibleAgeException(String message) {
        super(message);
    }
}