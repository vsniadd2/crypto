package com.difbriy.web.exception.dto;

import java.time.LocalDateTime;

public record AuthenticationCredentialsNotFoundExceptionDto(
        boolean success,
        String message,
        LocalDateTime localDateTime,
        String errorType
) {
    public static AuthenticationCredentialsNotFoundExceptionDto create(
            String message,
            String errorType
    ) {
        return new AuthenticationCredentialsNotFoundExceptionDto(
                false,
                message,
                LocalDateTime.now(),
                errorType
        );
    }
}
