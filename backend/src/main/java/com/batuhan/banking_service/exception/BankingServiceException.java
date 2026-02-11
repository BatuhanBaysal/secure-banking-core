package com.batuhan.banking_service.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class BankingServiceException extends RuntimeException {

    private final HttpStatus status;

    public BankingServiceException(String message) {
        super(message);
        this.status = HttpStatus.BAD_REQUEST;
    }

    public BankingServiceException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }
}