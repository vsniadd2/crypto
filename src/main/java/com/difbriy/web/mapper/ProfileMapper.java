package com.difbriy.web.mapper;

import com.difbriy.web.dto.user.ProfileDto;
import com.difbriy.web.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ProfileMapper {
    ProfileDto toDto(User user);
}
