package com.difbriy.web.exception.dto;

import java.time.LocalDateTime;

public record LogoutHandlerExceptionDto(
        boolean success,
        String message,
        LocalDateTime localDateTime,
        String errorType
) {
    public static LogoutHandlerExceptionDto create(String message, String errorType) {
        return new LogoutHandlerExceptionDto(
                false,
                message,
                LocalDateTime.now(),
                errorType
        );
    }
}




