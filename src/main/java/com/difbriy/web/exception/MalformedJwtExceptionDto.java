package com.difbriy.web.exception;

import java.time.LocalDateTime;

public record MalformedJwtExceptionDto(
        boolean success,
        String message,
        LocalDateTime localDateTime,
        String errorType
) {
    public static MalformedJwtExceptionDto create(String message, String errorType) {
        return new MalformedJwtExceptionDto(
                false,
                message,
                LocalDateTime.now(),
                errorType
        );
    }
}
