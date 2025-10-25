package com.difbriy.web.exception;

import java.time.LocalDateTime;

public record IllegalArgumentExceptionDto(
        boolean success,
        String message,
        LocalDateTime localDateTime,
        String errorType
) {
    public static IllegalArgumentExceptionDto create(String message, String errorType) {
        return new IllegalArgumentExceptionDto(
                false,
                message,
                LocalDateTime.now(),
                errorType
        );
    }
}
