package com.difbriy.web.exception;

import java.time.LocalDateTime;

public record ExpiredJwtExceptionDto(
        boolean success,
        String message,
        LocalDateTime localDateTime,
        String errorType
) {
    public static ExpiredJwtExceptionDto create(String message, String errorType) {
        return new ExpiredJwtExceptionDto(
                false,
                message,
                LocalDateTime.now(),
                errorType
        );
    }
}
