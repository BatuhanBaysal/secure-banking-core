package com.batuhan.banking_service.controller;

import com.batuhan.banking_service.constant.Messages;
import com.batuhan.banking_service.dto.common.GlobalResponse;
import com.batuhan.banking_service.dto.request.UserCreateRequest;
import com.batuhan.banking_service.dto.request.UserUpdateRequest;
import com.batuhan.banking_service.dto.response.UserResponse;
import com.batuhan.banking_service.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@Slf4j
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<GlobalResponse<UserResponse>> createUser(@Valid @RequestBody UserCreateRequest request) {
        log.info("API Request: Create user for email: {}", request.getEmail());
        UserResponse response = userService.createUser(request);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{customerNumber}")
                .buildAndExpand(response.getCustomerNumber())
                .toUri();

        return ResponseEntity.created(location)
                .body(GlobalResponse.success(response, Messages.USER_CREATED));
    }

    @PutMapping("/{customerNumber}")
    @PreAuthorize("hasRole('ADMIN') or @bankingBusinessValidator.isOwner(#customerNumber)")
    public ResponseEntity<GlobalResponse<UserResponse>> updateUser(
            @PathVariable String customerNumber,
            @Valid @RequestBody UserUpdateRequest request) {
        log.info("API Request: Update user: {}", customerNumber);
        UserResponse response = userService.updateUser(customerNumber, request);
        return ResponseEntity.ok(GlobalResponse.success(response, Messages.USER_UPDATED));
    }

    @GetMapping("/{customerNumber}")
    @PreAuthorize("hasRole('ADMIN') or @bankingBusinessValidator.isOwner(#customerNumber)")
    public ResponseEntity<GlobalResponse<UserResponse>> getUserByCustomerNumber(@PathVariable String customerNumber) {
        log.info("API Request: Get user details for: {}", customerNumber);
        UserResponse response = userService.getUserByCustomerNumber(customerNumber);
        return ResponseEntity.ok(GlobalResponse.success(response, Messages.USER_RETRIEVED));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<GlobalResponse<Page<UserResponse>>> getAllUsers(
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        log.info("API Request: Get all users, Page: {}", pageable.getPageNumber());
        Page<UserResponse> responses = userService.getAllUsers(pageable);
        return ResponseEntity.ok(GlobalResponse.success(responses, Messages.USERS_PAGINATED));
    }

    @DeleteMapping("/{customerNumber}")
    @PreAuthorize("hasRole('ADMIN') or @bankingBusinessValidator.isOwner(#customerNumber)")
    public ResponseEntity<GlobalResponse<Void>> deleteUser(@PathVariable String customerNumber) {
        log.warn("API Request: DEACTIVATE customer: {}", customerNumber);
        userService.deleteUser(customerNumber);
        return ResponseEntity.ok(GlobalResponse.success(null, Messages.USER_DEACTIVATED));
    }
}