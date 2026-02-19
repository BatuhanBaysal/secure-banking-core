package com.batuhan.banking_service.exception;

import com.batuhan.banking_service.dto.common.GlobalResponse;
import io.github.resilience4j.bulkhead.BulkheadFullException;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
@Hidden
public class GlobalExceptionHandler {

    @ExceptionHandler(BankingServiceException.class)
    ResponseEntity<GlobalResponse<Void>> handleBankingException(BankingServiceException ex) {
        log.warn("Business Logic Violation: {}", ex.getMessage());
        return ResponseEntity
                .status(ex.getStatus())
                .body(GlobalResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<GlobalResponse<Map<String, String>>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        fieldError -> fieldError.getDefaultMessage() != null ? fieldError.getDefaultMessage() : "Invalid value",
                        (existing, replacement) -> existing
                ));

        log.warn("Validation failed: {}", errors);
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(GlobalResponse.error("Validation failed", errors));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    ResponseEntity<GlobalResponse<Void>> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException ex) {
        Class<?> requiredType = ex.getRequiredType();
        String typeName = (requiredType != null) ? requiredType.getSimpleName() : "unknown";

        String message = String.format("Parameter '%s': Value '%s' is not of required type (%s)",
                ex.getName(), ex.getValue(), typeName);

        log.warn("Type mismatch error: {}", message);
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(GlobalResponse.error(message));
    }

    @ExceptionHandler(AuthorizationDeniedException.class)
    ResponseEntity<GlobalResponse<Void>> handleAccessDeniedException(AuthorizationDeniedException ex) {
        log.warn("Access denied: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(GlobalResponse.error("Access denied: You do not have permission to perform this action."));
    }

    @ExceptionHandler(RequestNotPermitted.class)
    ResponseEntity<GlobalResponse<Void>> handleRateLimiterException(RequestNotPermitted e) {
        log.warn("Rate limit exceeded: {}", e.getMessage());
        return ResponseEntity
                .status(HttpStatus.TOO_MANY_REQUESTS)
                .body(GlobalResponse.error("Too many requests. Please wait a moment before trying again."));
    }

    @ExceptionHandler(CallNotPermittedException.class)
    ResponseEntity<GlobalResponse<Void>> handleCircuitBreakerException(CallNotPermittedException e) {
        log.error("Circuit breaker is open: {}", e.getMessage());
        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(GlobalResponse.error("System is currently unavailable due to high load or maintenance. Please try later."));
    }

    @ExceptionHandler(BulkheadFullException.class)
    ResponseEntity<GlobalResponse<Void>> handleBulkheadException(BulkheadFullException e) {
        log.warn("Bulkhead limit reached: {}", e.getMessage());
        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(GlobalResponse.error("Server is busy processing heavy tasks. Please try again shortly."));
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<GlobalResponse<Void>> handleGeneralException(Exception ex) {
        log.error("Unexpected error occurred: ", ex);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(GlobalResponse.error("An unexpected error occurred. Please contact support."));
    }
}