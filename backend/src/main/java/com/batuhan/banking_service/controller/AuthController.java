package com.batuhan.banking_service.controller;

import com.batuhan.banking_service.dto.request.AuthRequest;
import com.batuhan.banking_service.dto.response.AuthResponse;
import com.batuhan.banking_service.dto.common.GlobalResponse;
import com.batuhan.banking_service.service.AuthService;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @SecurityRequirements
    public ResponseEntity<GlobalResponse<AuthResponse>> authenticate(@Valid @RequestBody AuthRequest request) {
        return ResponseEntity.ok(GlobalResponse.success(
                authService.authenticate(request),
                "Login successful")
        );
    }
}