package com.batuhan.banking_service.dto.response;

import com.batuhan.banking_service.dto.common.AddressDto;
import java.time.LocalDateTime;

public record UserResponse(
        Long id,
        String firstName,
        String lastName,
        String email,
        String customerNumber,
        AddressDto address,
        LocalDateTime createdAt
) {}