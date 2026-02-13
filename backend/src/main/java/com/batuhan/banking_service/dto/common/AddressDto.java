package com.batuhan.banking_service.dto.common;

import jakarta.validation.constraints.NotBlank;

public record AddressDto(
        @NotBlank(message = "City is required") String city,
        @NotBlank(message = "Street is required") String street,
        @NotBlank(message = "Zip code is required") String zipCode,
        @NotBlank(message = "Phone number is required") String phoneNumber
) {}