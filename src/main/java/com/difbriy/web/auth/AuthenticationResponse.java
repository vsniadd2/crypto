package com.difbriy.web.auth;

import com.difbriy.web.dto.UserDto;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record AuthenticationResponse(
        String accessToken,
        String refreshToken,
        boolean success,
        String message,
        UserDto user,
        LocalDateTime timestamp
) {
    public static AuthenticationResponse registration(String accessToken, String refreshToken, UserDto userDto) {
        String message = "Registration successful";
        return new AuthenticationResponse(
                accessToken,
                refreshToken,
                true,
                message,
                userDto,
                LocalDateTime.now()
        );
    }

    public static AuthenticationResponse login(String accessToken, String refreshToken, UserDto userDto) {
        String message = "Login successful";
        return new AuthenticationResponse(
                accessToken,
                refreshToken,
                true,
                message,
                userDto,
                LocalDateTime.now()
        );
    }

}
