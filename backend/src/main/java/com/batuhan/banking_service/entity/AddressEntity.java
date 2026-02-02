package com.batuhan.banking_service.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "addresses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddressEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String city;

    @Column(nullable = false)
    private String street;

    @Column(nullable = false, length = 10)
    private String zipCode;

    @Column(nullable = false, length = 15)
    private String phoneNumber;

    @Column(nullable = false)
    private boolean isActive = true;
}