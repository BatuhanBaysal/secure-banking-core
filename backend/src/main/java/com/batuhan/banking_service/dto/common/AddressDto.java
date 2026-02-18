package com.batuhan.banking_service.dto.common;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AddressDto(

        @NotBlank(message = "Country is required")
        @Size(min = 2, max = 50) String country,

        @NotBlank(message = "City is required")
        @Size(min = 2, max = 50) String city,

        @NotBlank(message = "District is required")
        @Size(min = 2, max = 50) String district,

        @NotBlank(message = "Street is required")
        @Size(min = 2, max = 100) String street,

        @NotBlank(message = "Building and Apartment info is required")
        @Size(max = 100) String addressDetail,

        @NotBlank(message = "Zip code is required")
        @Pattern(regexp = "^\\d{5}$", message = "Zip code must be 5 digits")
        String zipCode
) {}