package com.batuhan.banking_service.exception;

public class DailyLimitExceededException extends BankingServiceException {

    public DailyLimitExceededException(String message) {
        super(message);
    }
}