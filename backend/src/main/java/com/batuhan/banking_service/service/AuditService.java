package com.batuhan.banking_service.service;

public interface AuditService {

    void log(String action, String email, String details);
}