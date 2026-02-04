package com.batuhan.banking_service.dto.response;

import com.batuhan.banking_service.dto.common.AddressDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserResponse {

    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String customerNumber;
    private AddressDto address;
    private LocalDateTime createdAt;
}