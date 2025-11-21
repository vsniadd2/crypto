package com.difbriy.web.dto.user;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record PasswordResetResponseDto(
        String message,
        String resetToken,
        String resetUrl,
        LocalDateTime expiresAt,
        boolean success
) {
    
    public static PasswordResetResponseDto success(String token, String resetUrl, LocalDateTime expiresAt) {
        return PasswordResetResponseDto.builder()
                .success(true)
                .message("Ссылка для сброса пароля успешно создана")
                .resetToken(token)
                .resetUrl(resetUrl)
                .expiresAt(expiresAt)
                .build();
    }
    
    public static PasswordResetResponseDto error(String message) {
        return PasswordResetResponseDto.builder()
                .success(false)
                .message(message)
                .build();
    }
    
    public static PasswordResetResponseDto passwordChanged() {
        return PasswordResetResponseDto.builder()
                .success(true)
                .message("Пароль успешно изменен")
                .build();
    }
}
