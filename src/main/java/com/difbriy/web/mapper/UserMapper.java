package com.difbriy.web.mapper;

import com.difbriy.web.auth.RegistrationRequest;
import com.difbriy.web.dto.user.UpdatedProfileResponseDto;
import com.difbriy.web.dto.user.UserDto;
import com.difbriy.web.entity.User;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Indexed;

import java.util.List;

import static com.difbriy.web.roles.Role.ROLE_USER;


@Component
@RequiredArgsConstructor
public class UserMapper {
    private final PasswordEncoder passwordEncoder;

    public User toEntity(RegistrationRequest request) {
        return User.builder()
                .username(request.username())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .roles(List.of(ROLE_USER))
                .build();
    }

    public UserDto toDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .build();
    }

    public UpdatedProfileResponseDto toUpdatedProfileDto(User user, String token) {
        return UpdatedProfileResponseDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .token(token)
                .build();
    }
}
