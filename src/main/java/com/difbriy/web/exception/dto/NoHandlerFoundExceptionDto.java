package com.difbriy.web.exception.dto;

import java.time.LocalDateTime;

public record NoHandlerFoundExceptionDto(
        boolean success,
        String message,
        LocalDateTime localDateTime,
        String errorType
) {
    public static NoHandlerFoundExceptionDto create(String message, String errorType) {
        return new NoHandlerFoundExceptionDto(
                false,
                message,
                LocalDateTime.now(),
                errorType
        );
    }
}
