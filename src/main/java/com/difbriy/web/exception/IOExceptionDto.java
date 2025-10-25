package com.difbriy.web.exception;

import java.time.LocalDateTime;

public record IOExceptionDto(
        boolean success,
        String message,
        LocalDateTime localDateTime,
        String errorType
) {
    public static IOExceptionDto create(String message, String errorType) {
        return new IOExceptionDto(
                false,
                message,
                LocalDateTime.now(),
                errorType
        );
    }
}
