package com.batuhan.banking_service.mapper;

import com.batuhan.banking_service.dto.request.UserCreateRequest;
import com.batuhan.banking_service.dto.response.UserResponse;
import com.batuhan.banking_service.dto.request.UserUpdateRequest;
import com.batuhan.banking_service.entity.UserEntity;
import org.mapstruct.*;

@Mapper(componentModel = "spring", uses = {AddressMapper.class})
public interface UserMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "externalId", ignore = true)
    @Mapping(target = "customerNumber", ignore = true)
    @Mapping(target = "accounts", ignore = true)
    @Mapping(target = "role", constant = "USER")
    @Mapping(target = "active", constant = "true")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "lastModifiedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    UserEntity toEntity(UserCreateRequest request);

    UserResponse toResponse(UserEntity user);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "externalId", ignore = true)
    @Mapping(target = "customerNumber", ignore = true)
    @Mapping(target = "tckn", ignore = true)
    @Mapping(target = "birthDate", ignore = true)
    @Mapping(target = "accounts", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "lastModifiedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    void updateEntityFromDto(UserUpdateRequest request, @MappingTarget UserEntity user);
}