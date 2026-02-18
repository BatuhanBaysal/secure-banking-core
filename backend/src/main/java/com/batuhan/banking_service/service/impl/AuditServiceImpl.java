package com.batuhan.banking_service.service.impl;

import com.batuhan.banking_service.entity.AuditLogEntity;
import com.batuhan.banking_service.repository.AuditLogRepository;
import com.batuhan.banking_service.service.AuditService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditServiceImpl implements AuditService {

    private final AuditLogRepository auditLogRepository;

    @Async("auditTaskExecutor")
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(String action, String email, String details) {
        String clientIp = getClientIp();

        try {
            AuditLogEntity auditEntry = AuditLogEntity.builder()
                    .action(action)
                    .email(email)
                    .details(details)
                    .ipAddress(clientIp)
                    .build();

            auditLogRepository.save(auditEntry);
            log.debug("Audit log saved. Action: {}, User: {}, IP: {}", action, email, clientIp);

        } catch (Exception e) {
            log.error("CRITICAL: Audit log could not be saved! Action: {}, User: {}, Error: {}",
                    action, email, e.getMessage());
        }
    }

    private String getClientIp() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String xfHeader = request.getHeader("X-Forwarded-For");
                if (xfHeader == null) {
                    return request.getRemoteAddr();
                }
                return xfHeader.split(",")[0];
            }
        } catch (Exception e) {
            log.warn("Could not determine client IP for audit log");
        }
        return "unknown";
    }
}