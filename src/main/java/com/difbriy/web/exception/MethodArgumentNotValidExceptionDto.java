package com.difbriy.web.exception;

import java.time.LocalDateTime;
import java.util.Map;

public record MethodArgumentNotValidExceptionDto(
        boolean success,
        String message,
        Map<String, String> errors,
        LocalDateTime localDateTime,
        String errorType
) {
    public static MethodArgumentNotValidExceptionDto create(String message, Map<String, String> errors) {
        return new MethodArgumentNotValidExceptionDto(
                false,
                message,
                errors,
                LocalDateTime.now(),
                "VALIDATION_ERROR"
        );
    }
}
