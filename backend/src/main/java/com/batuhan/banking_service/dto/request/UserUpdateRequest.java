package com.batuhan.banking_service.dto.request;

import com.batuhan.banking_service.dto.common.AddressDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

public record UserUpdateRequest(

        @NotBlank(message = "First name is required")
        @Size(min = 2, max = 50, message = "First name must be between 2-50 characters")
        String firstName,

        @NotBlank(message = "Last name is required")
        @Size(min = 2, max = 50, message = "Last name must be between 2-50 characters")
        String lastName,

        @NotBlank(message = "Email is required")
        @Email(message = "Please provide a valid email address")
        String email,

        @NotBlank(message = "Phone number is required")
        @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Invalid phone number format")
        String phoneNumber,

        @NotNull(message = "Address information is required")
        @Valid
        AddressDto address
) {}