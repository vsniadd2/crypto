package com.difbriy.web.exception;

import java.time.LocalDateTime;

public record ExceptionDto(
        boolean success,
        String message,
        LocalDateTime localDateTime,
        String errorType
) {
    public static ExceptionDto create(String message, String errorType) {
        return new ExceptionDto(
                false,
                message,
                LocalDateTime.now(),
                errorType
        );
    }
}
