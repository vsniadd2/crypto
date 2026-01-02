package com.difbriy.web.exception.dto;

import java.time.LocalDateTime;

public record ImageProcessingExceptionDto(
        boolean success,
        String message,
        LocalDateTime localDateTime,
        String errorType
) {
    public static ImageProcessingExceptionDto create(String message, String errorType) {
        return new ImageProcessingExceptionDto(
                false,
                message,
                LocalDateTime.now(),
                errorType
        );
    }
}
