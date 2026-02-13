package com.batuhan.banking_service.dto.request;

import com.batuhan.banking_service.dto.common.AddressDto;
import com.batuhan.banking_service.validator.MinAge;
import com.batuhan.banking_service.validator.ValidTckn;
import com.batuhan.banking_service.validator.ValidPassword;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.time.LocalDate;

public record UserCreateRequest(
        @NotBlank @Size(min = 2, max = 50) String firstName,
        @NotBlank @Size(min = 2, max = 50) String lastName,
        @NotBlank @ValidTckn String tckn,
        @NotBlank @Email String email,
        @NotBlank @ValidPassword String password,
        @NotNull @MinAge(value = 18) LocalDate birthDate,
        @NotNull @Valid AddressDto address
) {}