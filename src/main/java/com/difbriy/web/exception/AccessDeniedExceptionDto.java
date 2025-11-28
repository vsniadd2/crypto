package com.difbriy.web.exception;

import java.time.LocalDateTime;

public record AccessDeniedExceptionDto(
        boolean success,
        String message,
        LocalDateTime localDateTime,
        String errorType
) {
    public static AccessDeniedExceptionDto create(String message, String errorType) {
        return new AccessDeniedExceptionDto(
                false,
                message,
                LocalDateTime.now(),
                errorType
        );
    }
}

