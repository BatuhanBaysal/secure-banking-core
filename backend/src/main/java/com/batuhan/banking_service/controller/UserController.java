package com.batuhan.banking_service.controller;

import com.batuhan.banking_service.constant.Messages;
import com.batuhan.banking_service.dto.common.GlobalResponse;
import com.batuhan.banking_service.dto.request.UserCreateRequest;
import com.batuhan.banking_service.dto.request.UserUpdateRequest;
import com.batuhan.banking_service.dto.response.UserResponse;
import com.batuhan.banking_service.service.UserService;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "User Management", description = "Operations related to customer profiles and system users")
public class UserController {

    private final UserService userService;

    @PostMapping
    @Operation(summary = "Register a new user", description = "Public endpoint to create a new banking customer")
    @RateLimiter(name = "userCreationLimiter")
    public ResponseEntity<GlobalResponse<UserResponse>> createUser(@Valid @RequestBody UserCreateRequest request) {
        log.info("API Request: Create user for email: {}", request.email());
        UserResponse response = userService.createUser(request);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{customerNumber}")
                .buildAndExpand(response.customerNumber())
                .toUri();

        return ResponseEntity.created(location)
                .body(GlobalResponse.success(response, Messages.USER_CREATED));
    }

    @PutMapping("/{customerNumber}")
    @Operation(summary = "Update user details", description = "Requires ADMIN role or to be the profile owner")
    @PreAuthorize("hasRole('ADMIN') or @bankingBusinessValidator.isOwner(#customerNumber)")
    public ResponseEntity<GlobalResponse<UserResponse>> updateUser(
            @PathVariable("customerNumber") String customerNumber,
            @Valid @RequestBody UserUpdateRequest request) {
        log.info("API Request: Update user: {}", customerNumber);
        UserResponse response = userService.updateUser(customerNumber, request);
        return ResponseEntity.ok(GlobalResponse.success(response, Messages.USER_UPDATED));
    }

    @GetMapping("/{customerNumber}")
    @Operation(summary = "Get user by customer number")
    @PreAuthorize("hasRole('ADMIN') or @bankingBusinessValidator.isOwner(#customerNumber)")
    public ResponseEntity<GlobalResponse<UserResponse>> getUserByCustomerNumber(@PathVariable String customerNumber) {
        log.info("API Request: Get user details for: {}", customerNumber);
        UserResponse response = userService.getUserByCustomerNumber(customerNumber);
        return ResponseEntity.ok(GlobalResponse.success(response, Messages.USER_RETRIEVED));
    }

    @GetMapping
    @Operation(summary = "List all users (Admin Only)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<GlobalResponse<Page<UserResponse>>> getAllUsers(
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        log.info("API Request: Get all users, Page: {}", pageable.getPageNumber());
        Page<UserResponse> responses = userService.getAllUsers(pageable);
        return ResponseEntity.ok(GlobalResponse.success(responses, Messages.USERS_PAGINATED));
    }

    @GetMapping("/me/roles")
    @Operation(summary = "Debug: Check current session roles and claims")
    public ResponseEntity<?> getMyRoles() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        Map<String, Object> details = new LinkedHashMap<>();

        if (auth instanceof JwtAuthenticationToken jwtAuth) {
            details.put("user_id", jwtAuth.getName());
            details.put("email_in_token", jwtAuth.getToken().getClaim("email"));
            details.put("all_claims", jwtAuth.getToken().getClaims());
        }

        details.put("granted_authorities", auth.getAuthorities());
        return ResponseEntity.ok(details);
    }

    @DeleteMapping("/{customerNumber}")
    @Operation(summary = "Deactivate user", description = "Performs a soft delete/deactivation")
    @PreAuthorize("hasRole('ADMIN') or @bankingBusinessValidator.isOwner(#customerNumber)")
    public ResponseEntity<GlobalResponse<Void>> deleteUser(@PathVariable String customerNumber) {
        log.warn("API Request: DEACTIVATE customer: {}", customerNumber);
        userService.deleteUser(customerNumber);
        return ResponseEntity.ok(GlobalResponse.success(null, Messages.USER_DEACTIVATED));
    }
}