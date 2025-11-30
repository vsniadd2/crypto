package com.difbriy.web.dto.contact;


import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record ContactDto(
        Long id,
        String name,
        String email,
        String subject,
        String message,
        LocalDateTime timestamp
) {
}
