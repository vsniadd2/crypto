package com.difbriy.web.dto;

import lombok.Builder;

@Builder
public record UserDto(
        Long id,
        String username,
        String email
) {
}
