package com.difbriy.web.exception.dto;

import java.time.LocalDateTime;

public record BadCredentialsExceptionDto(
        boolean success,
        String message,
        LocalDateTime localDateTime,
        String errorType
) {
    public static BadCredentialsExceptionDto create(String message, String errorType) {
        return new BadCredentialsExceptionDto(
                false,
                message,
                LocalDateTime.now(),
                errorType
        );
    }
}
