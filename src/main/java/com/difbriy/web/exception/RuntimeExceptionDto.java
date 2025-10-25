package com.difbriy.web.exception;

import java.time.LocalDateTime;

public record RuntimeExceptionDto(
        boolean success,
        String message,
        LocalDateTime localDateTime,
        String errorType
) {
    public static RuntimeExceptionDto create(
            String message,
            String errorType
    ) {
        return new RuntimeExceptionDto(
                false,
                message,
                LocalDateTime.now(),
                errorType
        );
    }

}
