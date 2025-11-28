package com.difbriy.web.exception;

import java.time.LocalDateTime;

public record AuthenticationEntryPointExceptionDto(
        boolean success,
        String message,
        LocalDateTime localDateTime,
        String errorType
) {
    public static AuthenticationEntryPointExceptionDto create(String message, String errorType) {
        return new AuthenticationEntryPointExceptionDto(
                false,
                message,
                LocalDateTime.now(),
                errorType
        );
    }
}

