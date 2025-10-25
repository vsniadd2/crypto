package com.difbriy.web.exception;

import java.time.LocalDateTime;

public record UsernameNotFoundExceptionDto(
        boolean success,
        String message,
        LocalDateTime localDateTime,
        String errorType
) {
    public static UsernameNotFoundExceptionDto create(String message, String errorType) {
        return new UsernameNotFoundExceptionDto(
                false,
                message,
                LocalDateTime.now(),
                errorType
        );
    }
}
