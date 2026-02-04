package com.batuhan.banking_service.dto.request;

import com.batuhan.banking_service.dto.common.AddressDto;
import com.batuhan.banking_service.validator.MinAge;
import com.batuhan.banking_service.validator.ValidTckn;
import com.batuhan.banking_service.validator.ValidPassword;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserCreateRequest {

    @NotBlank(message = "First name is required")
    @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters")
    private String lastName;

    @NotBlank(message = "TCKN is required")
    @ValidTckn
    private String tckn;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Password is required")
    @ValidPassword
    private String password;

    @NotNull(message = "Birth date is required")
    @MinAge(value = 18)
    private LocalDate birthDate;

    @NotNull(message = "Address information is required")
    @Valid
    private AddressDto address;
}