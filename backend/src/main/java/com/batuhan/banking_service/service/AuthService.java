package com.batuhan.banking_service.service;

import com.batuhan.banking_service.dto.request.AuthRequest;
import com.batuhan.banking_service.dto.response.AuthResponse;

public interface AuthService {

    AuthResponse authenticate(AuthRequest request);
}