package com.batuhan.banking_service.repository;

import com.batuhan.banking_service.entity.AuditLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLogEntity, Long> {

    List<AuditLogEntity> findByEmailOrderByCreatedAtDesc(String email);
}