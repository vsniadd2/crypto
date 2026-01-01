package com.difbriy.web.mapper;

import com.difbriy.web.auth.RegistrationRequest;
import com.difbriy.web.dto.user.UpdatedProfileResponseDto;
import com.difbriy.web.dto.user.UserDto;
import com.difbriy.web.entity.User;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

import static com.difbriy.web.roles.Role.ROLE_USER;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, uses = {PasswordEncoder.class})
public interface UserMapper {

    @Mapping(target = "password", expression = "java(passwordEncoder.encode(request.password()))")
    @Mapping(target = "roles", expression = "java(java.util.List.of(com.difbriy.web.roles.Role.ROLE_USER))")
    @Mapping(target = "isActive", constant = "true")
    User toEntity(RegistrationRequest request, @Context PasswordEncoder passwordEncoder);

    UserDto toDto(User user);

    UpdatedProfileResponseDto toUpdatedProfileDto(User user, @Context String token);
}