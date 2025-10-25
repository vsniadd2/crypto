package com.difbriy.web.exception;

import java.time.LocalDateTime;

public record SignatureExceptionDto(
        boolean success,
        String message,
        LocalDateTime localDateTime,
        String errorType
) {
    public static SignatureExceptionDto create(String message, String errorType) {
        return new SignatureExceptionDto(
                false,
                message,
                LocalDateTime.now(),
                errorType
        );
    }
}
