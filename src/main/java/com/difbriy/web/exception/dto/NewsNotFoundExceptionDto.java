package com.difbriy.web.exception.dto;

import java.time.LocalDateTime;

public record NewsNotFoundExceptionDto(
        boolean success,
        String message,
        LocalDateTime localDateTime,
        String errorType
) {
    public static NewsNotFoundExceptionDto create(String message, String errorType) {
        return new NewsNotFoundExceptionDto(
                false,
                message,
                LocalDateTime.now(),
                errorType
        );
    }
}
