package com.batuhan.banking_service.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "audit_logs", indexes = {
        @Index(name = "idx_audit_email", columnList = "email"),
        @Index(name = "idx_audit_action", columnList = "action")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class AuditLogEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String action;

    @Column(nullable = false)
    private String email;

    @Column(columnDefinition = "TEXT")
    private String details;

    private String ipAddress;
}