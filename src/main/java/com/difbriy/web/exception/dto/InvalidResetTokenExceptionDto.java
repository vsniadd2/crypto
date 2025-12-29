package com.difbriy.web.exception.dto;

import java.time.LocalDateTime;

public record InvalidResetTokenExceptionDto(
        boolean success,
        String message,
        LocalDateTime localDateTime,
        String errorType
) {
    public static InvalidResetTokenExceptionDto create(String message, String errorType) {
        return new InvalidResetTokenExceptionDto(
                false,
                message,
                LocalDateTime.now(),
                errorType
        );
    }
}

