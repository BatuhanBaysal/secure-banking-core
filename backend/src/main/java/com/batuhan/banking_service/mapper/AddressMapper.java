package com.batuhan.banking_service.mapper;

import com.batuhan.banking_service.dto.common.AddressDto;
import com.batuhan.banking_service.entity.AddressEntity;
import org.springframework.stereotype.Component;

@Component
public class AddressMapper {

    public AddressEntity toEntity(AddressDto dto) {
        if (dto == null) return null;

        return AddressEntity.builder()
                .city(dto.getCity())
                .street(dto.getStreet())
                .zipCode(dto.getZipCode())
                .phoneNumber(dto.getPhoneNumber())
                .isActive(true)
                .build();
    }

    public AddressDto toDto(AddressEntity entity) {
        if (entity == null) return null;

        return AddressDto.builder()
                .city(entity.getCity())
                .street(entity.getStreet())
                .zipCode(entity.getZipCode())
                .phoneNumber(entity.getPhoneNumber())
                .build();
    }

    public void updateEntityFromDto(AddressDto dto, AddressEntity entity) {
        if (dto == null || entity == null) return;

        entity.setCity(dto.getCity());
        entity.setStreet(dto.getStreet());
        entity.setZipCode(dto.getZipCode());
        entity.setPhoneNumber(dto.getPhoneNumber());
    }
}