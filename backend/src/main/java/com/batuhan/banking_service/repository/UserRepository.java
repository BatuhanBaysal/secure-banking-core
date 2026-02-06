package com.batuhan.banking_service.repository;

import com.batuhan.banking_service.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {

    Optional<UserEntity> findByCustomerNumber(String customerNumber);
    Optional<UserEntity> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByTckn(String tckn);
    boolean existsByCustomerNumber(String customerNumber);
    List<UserEntity> findAllByIsActiveTrue();
}