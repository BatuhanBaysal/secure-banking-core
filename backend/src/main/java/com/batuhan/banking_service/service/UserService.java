package com.batuhan.banking_service.service;

import com.batuhan.banking_service.dto.request.UserCreateRequest;
import com.batuhan.banking_service.dto.request.UserUpdateRequest;
import com.batuhan.banking_service.dto.response.UserResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {

    UserResponse createUser(UserCreateRequest request);
    UserResponse updateUser(String customerNumber, UserUpdateRequest request);
    UserResponse getUserByCustomerNumber(String customerNumber);
    Page<UserResponse> getAllUsers(Pageable pageable);
    void deleteUser(String customerNumber);
}