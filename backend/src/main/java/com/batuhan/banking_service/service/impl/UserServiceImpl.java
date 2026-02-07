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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final BankingBusinessValidator businessValidator;

    @Override
    @Transactional
    public UserResponse createUser(UserCreateRequest request) {
        log.info("Creating user with email: {}", request.getEmail());
        validateUserUniqueness(request);

        UserEntity userEntity = userMapper.toEntity(request);
        userEntity.setPassword(passwordEncoder.encode(request.getPassword()));
        userEntity.setCustomerNumber(generateUniqueCustomerNumber());
        userEntity.setActive(true);

        UserEntity savedUser = userRepository.save(userEntity);
        log.info("User created successfully with Customer No: {}", savedUser.getCustomerNumber());
        return userMapper.toResponse(savedUser);
    }

    @Override
    @Transactional
    public UserResponse updateUser(String customerNumber, UserUpdateRequest request) {
        log.info("Updating profile for customer: {}", customerNumber);
        UserEntity userEntity = findEntityByCustomerNumber(customerNumber);

        businessValidator.validateOwnership(userEntity);
        validateEmailForUpdate(userEntity, request.getEmail());
        userMapper.updateEntityFromRequest(request, userEntity);

        return userMapper.toResponse(userRepository.save(userEntity));
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserByCustomerNumber(String customerNumber) {
        UserEntity userEntity = findEntityByCustomerNumber(customerNumber);
        businessValidator.validateOwnership(userEntity);
        return userMapper.toResponse(userEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserResponse> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable).map(userMapper::toResponse);
    }

    @Override
    @Transactional
    public void deleteUser(String customerNumber) {
        log.warn("Initiating deactivation for customer: {}", customerNumber);
        UserEntity user = findEntityByCustomerNumber(customerNumber);

        businessValidator.validateOwnership(user);

        user.setActive(false);
        userRepository.save(user);
    }

    private UserEntity findEntityByCustomerNumber(String customerNumber) {
        return userRepository.findByCustomerNumber(customerNumber)
                .orElseThrow(() -> new BankingServiceException("Customer not found: " + customerNumber, HttpStatus.NOT_FOUND));
    }

    private void validateUserUniqueness(UserCreateRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BankingServiceException("Email already in use", HttpStatus.CONFLICT);
        }
        if (userRepository.existsByTckn(request.getTckn())) {
            throw new BankingServiceException("TCKN already in use", HttpStatus.CONFLICT);
        }
    }

    private void validateEmailForUpdate(UserEntity user, String newEmail) {
        if (!user.getEmail().equals(newEmail) && userRepository.existsByEmail(newEmail)) {
            throw new BankingServiceException("New email is already taken by another user", HttpStatus.CONFLICT);
        }
    }

    private String generateUniqueCustomerNumber() {
        String number;
        do {
            number = String.valueOf(ThreadLocalRandom.current().nextLong(1000000000L, 10000000000L));
        } while (userRepository.existsByCustomerNumber(number));
        return number;
    }
}