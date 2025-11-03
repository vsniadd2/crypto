package com.difbriy.web.dto.user;

import lombok.Builder;

@Builder
public record UpdatedProfileResponseDto(
        Long id,
        String username,
        String email,
        String token
) {
}
