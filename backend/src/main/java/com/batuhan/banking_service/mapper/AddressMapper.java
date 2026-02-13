package com.batuhan.banking_service.mapper;

import com.batuhan.banking_service.dto.common.AddressDto;
import com.batuhan.banking_service.entity.AddressEntity;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface AddressMapper {

    AddressEntity toEntity(AddressDto dto);

    AddressDto toDto(AddressEntity entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDto(AddressDto dto, @MappingTarget AddressEntity entity);
}