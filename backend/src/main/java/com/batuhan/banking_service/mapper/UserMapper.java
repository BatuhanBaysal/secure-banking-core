package com.batuhan.banking_service.mapper;

import com.batuhan.banking_service.dto.request.UserCreateRequest;
import com.batuhan.banking_service.dto.response.UserResponse;
import com.batuhan.banking_service.dto.request.UserUpdateRequest;
import com.batuhan.banking_service.entity.UserEntity;
import org.mapstruct.*;

@Mapper(componentModel = "spring", uses = {AddressMapper.class})
public interface UserMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "customerNumber", ignore = true)
    @Mapping(target = "accounts", ignore = true)
    @Mapping(target = "role", constant = "USER")
    @Mapping(target = "isActive", constant = "true")
    UserEntity toEntity(UserCreateRequest request);

    @Mapping(target = "createdAt", source = "createdAt")
    UserResponse toResponse(UserEntity user);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromRequest(UserUpdateRequest request, @MappingTarget UserEntity entity);
}