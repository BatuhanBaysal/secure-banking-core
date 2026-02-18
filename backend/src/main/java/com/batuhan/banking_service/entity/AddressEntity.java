package com.batuhan.banking_service.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "addresses", indexes = {
        @Index(name = "idx_address_external_id", columnList = "externalId"),
        @Index(name = "idx_address_city", columnList = "city"),
        @Index(name = "idx_address_district", columnList = "district")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class AddressEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Builder.Default
    @Column(unique = true, nullable = false)
    private UUID externalId = UUID.randomUUID();

    @Column(nullable = false, length = 50)
    private String country;

    @Column(nullable = false, length = 50)
    private String city;

    @Column(nullable = false, length = 50)
    private String district;

    @Column(nullable = false, length = 100)
    private String street;

    @Column(nullable = false, length = 255)
    private String addressDetail;

    @Column(nullable = false, length = 20)
    private String zipCode;

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @OneToMany(mappedBy = "address")
    @Builder.Default
    private List<UserEntity> users = new ArrayList<>();
}