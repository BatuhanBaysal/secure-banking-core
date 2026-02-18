package com.batuhan.banking_service.dto.request;

import com.batuhan.banking_service.dto.common.AddressDto;
import com.batuhan.banking_service.validator.MinAge;
import com.batuhan.banking_service.validator.ValidTckn;
import com.batuhan.banking_service.validator.ValidPassword;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.time.LocalDate;

public record UserCreateRequest(

        @NotBlank(message = "First name cannot be empty")
        @Size(min = 2, max = 50, message = "First name must be between 2-50 characters")
        String firstName,

        @NotBlank(message = "Last name cannot be empty")
        String lastName,

        @NotBlank(message = "TCKN is required")
        @ValidTckn
        String tckn,

        @NotBlank(message = "Email is required")
        @Email(message = "Please provide a valid email address")
        String email,

        @NotBlank(message = "Password is required")
        @ValidPassword
        String password,

        @NotNull(message = "Birth date is required")
        @MinAge(value = 18, message = "You must be at least 18 years old")
        LocalDate birthDate,

        @NotNull(message = "Address information is missing")
        @Valid
        AddressDto address,

        @NotBlank(message = "Phone number is required")
        @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Phone number must be in international format (e.g., +905...)")
        String phoneNumber
) {}