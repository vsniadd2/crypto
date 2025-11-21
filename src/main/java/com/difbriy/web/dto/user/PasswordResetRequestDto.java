package com.difbriy.web.dto.user;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record PasswordResetRequestDto(
        @NotBlank
        String email
) {
}
