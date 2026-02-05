package com.batuhan.banking_service.mapper;

import com.batuhan.banking_service.dto.request.UserCreateRequest;
import com.batuhan.banking_service.dto.response.UserResponse;
import com.batuhan.banking_service.dto.request.UserUpdateRequest;
import com.batuhan.banking_service.entity.UserEntity;
import com.batuhan.banking_service.entity.enums.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserMapper {

    private final AddressMapper addressMapper;

    public UserEntity toEntity(UserCreateRequest request) {
        return UserEntity.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .tckn(request.getTckn())
                .birthDate(request.getBirthDate())
                .address(addressMapper.toEntity(request.getAddress()))
                .isActive(true)
                .role(Role.USER)
                .build();
    }

    public UserResponse toResponse(UserEntity user) {
        if (user == null) return null;

        return UserResponse.builder()
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .customerNumber(user.getCustomerNumber())
                .address(addressMapper.toDto(user.getAddress()))
                .build();
    }

    public void updateEntityFromRequest(UserUpdateRequest request, UserEntity entity) {
        entity.setFirstName(request.getFirstName());
        entity.setLastName(request.getLastName());
        entity.setEmail(request.getEmail());

        if (request.getAddress() != null) {
            if (entity.getAddress() == null) {
                entity.setAddress(addressMapper.toEntity(request.getAddress()));
            } else {
                addressMapper.updateEntityFromDto(request.getAddress(), entity.getAddress());
            }
        }
    }
}