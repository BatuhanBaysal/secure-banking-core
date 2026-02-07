package com.batuhan.banking_service.service.impl;

import com.batuhan.banking_service.entity.AuditLogEntity;
import com.batuhan.banking_service.repository.AuditLogRepository;
import com.batuhan.banking_service.service.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuditServiceImpl implements AuditService {

    private final AuditLogRepository auditLogRepository;

    @Override
    public void log(String action, String email, String details) {
        AuditLogEntity log = AuditLogEntity.builder()
                .action(action)
                .email(email)
                .details(details)
                .build();
        auditLogRepository.save(log);
    }
}