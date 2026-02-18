package com.batuhan.banking_service.entity;

import com.batuhan.banking_service.entity.enums.Role;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users", indexes = {
        @Index(name = "idx_user_external_id", columnList = "externalId"),
        @Index(name = "idx_user_email", columnList = "email"),
        @Index(name = "idx_user_customer_no", columnList = "customerNumber")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class UserEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, updatable = false)
    private String externalId;

    @Column(nullable = false, length = 50)
    private String firstName;

    @Column(nullable = false, length = 50)
    private String lastName;

    @Column(unique = true, nullable = false, length = 11)
    private String tckn;

    @Column(unique = true, nullable = false, length = 10)
    private String customerNumber;

    @Column(unique = true, nullable = false, length = 100)
    private String email;

    @Column(nullable = false)
    private LocalDate birthDate;

    @Column(unique = true, nullable = false, length = 15)
    private String phoneNumber;

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "address_id")
    private AddressEntity address;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<AccountEntity> accounts = new ArrayList<>();
}