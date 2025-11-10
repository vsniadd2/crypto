package com.difbriy.web.mapper;

import com.difbriy.web.dto.user.ProfileDto;
import com.difbriy.web.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProfileMapper {

    public ProfileDto toDto(User user){
        return ProfileDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .dateTimeOfCreated(user.getDateTimeOfCreated())
                .build();
    }
}
