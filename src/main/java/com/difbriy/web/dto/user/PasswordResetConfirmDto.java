package com.difbriy.web.dto.user;

import lombok.Builder;

@Builder
public record PasswordResetConfirmDto(
        String token,
        String newPassword,
        String confirmPassword
) {
}
