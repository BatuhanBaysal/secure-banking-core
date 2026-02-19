package com.batuhan.banking_service.service.impl;

import com.batuhan.banking_service.dto.request.UserCreateRequest;
import com.batuhan.banking_service.dto.request.UserUpdateRequest;
import com.batuhan.banking_service.dto.response.UserResponse;
import com.batuhan.banking_service.entity.UserEntity;
import com.batuhan.banking_service.exception.BankingServiceException;
import com.batuhan.banking_service.mapper.UserMapper;
import com.batuhan.banking_service.repository.UserRepository;
import com.batuhan.banking_service.service.UserService;
import com.batuhan.banking_service.service.helper.BankingBusinessValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final BankingBusinessValidator businessValidator;
    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    @Transactional
    public UserResponse createUser(UserCreateRequest request) {
        log.info("Synchronizing Keycloak user to local database: {}", request.email());
        validateUserUniqueness(request);

        UserEntity userEntity = userMapper.toEntity(request);
        userEntity.setCustomerNumber(generateUniqueCustomerNumber());
        userEntity.setActive(true);

        UserEntity savedUser = userRepository.save(userEntity);
        log.info("User created successfully with Customer No: {}", savedUser.getCustomerNumber());
        return userMapper.toResponse(savedUser);
    }

    @Override
    @Transactional
    @CachePut(value = "users", key = "#customerNumber")
    public UserResponse updateUser(String customerNumber, UserUpdateRequest request) {
        log.info("Updating profile for customer: {}", customerNumber);

        UserEntity userEntity = findEntityByCustomerNumber(customerNumber);
        businessValidator.validateOwnership(userEntity);

        validateEmailForUpdate(userEntity, request.email());
        userMapper.updateEntityFromDto(request, userEntity);

        UserEntity updatedUser = userRepository.save(userEntity);
        return userMapper.toResponse(updatedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserByCustomerNumber(String customerNumber) {
        log.info("Fetching user details for Customer No: {}", customerNumber);
        UserEntity userEntity = findEntityByCustomerNumber(customerNumber);
        businessValidator.validateOwnership(userEntity);
        return userMapper.toResponse(userEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserResponse> getAllUsers(Pageable pageable) {
        log.debug("Listing active users. Page: {}", pageable.getPageNumber());
        return userRepository.findAll(pageable).map(userMapper::toResponse);
    }

    @Override
    @Transactional
    @CacheEvict(value = "users", key = "#customerNumber")
    public void deleteUser(String customerNumber) {
        log.warn("Initiating user deactivation: {}", customerNumber);
        UserEntity user = findEntityByCustomerNumber(customerNumber);

        businessValidator.validateOwnership(user);

        user.setActive(false);
        userRepository.save(user);
        log.info("Customer {} successfully deactivated", customerNumber);
    }

    private UserEntity findEntityByCustomerNumber(String customerNumber) {
        return userRepository.findByCustomerNumber(customerNumber)
                .orElseThrow(() -> new BankingServiceException("User not found with Customer No: " + customerNumber, HttpStatus.NOT_FOUND));
    }

    private void validateUserUniqueness(UserCreateRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new BankingServiceException("Email is already in use: " + request.email(), HttpStatus.CONFLICT);
        }
        if (userRepository.existsByTckn(request.tckn())) {
            throw new BankingServiceException("TCKN is already in use", HttpStatus.CONFLICT);
        }
    }

    private void validateEmailForUpdate(UserEntity user, String newEmail) {
        if (newEmail != null && !user.getEmail().equalsIgnoreCase(newEmail) && userRepository.existsByEmail(newEmail)) {
            throw new BankingServiceException("New email is already taken by another user", HttpStatus.CONFLICT);
        }
    }

    private String generateUniqueCustomerNumber() {
        String customerNumber;
        boolean exists;
        do {
            long min = 1000000000L;
            long max = 9999999999L;
            long range = max - min + 1;
            long randomValue = min + (long) (secureRandom.nextDouble() * range);

            customerNumber = String.valueOf(randomValue);
            exists = userRepository.existsByCustomerNumber(customerNumber);
        } while (exists);

        return customerNumber;
    }
}