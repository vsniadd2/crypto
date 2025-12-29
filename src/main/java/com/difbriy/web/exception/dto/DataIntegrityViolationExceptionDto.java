package com.difbriy.web.exception.dto;

import java.time.LocalDateTime;

public record DataIntegrityViolationExceptionDto(
        boolean success,
        String message,
        String detailMessage,
        LocalDateTime localDateTime,
        String errorType
) {
    public static DataIntegrityViolationExceptionDto create(String message, String errorType) {
        String detailMessage = "Database constraint violation";
        return new DataIntegrityViolationExceptionDto(
                false,
                message,
                detailMessage,
                LocalDateTime.now(),
                errorType
        );
    }
}
