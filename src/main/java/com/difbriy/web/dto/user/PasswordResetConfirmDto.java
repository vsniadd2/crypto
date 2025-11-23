package com.difbriy.web.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record PasswordResetConfirmDto(
        @NotBlank(message = "Токен не может быть пустым")
        String token,
        
        @NotBlank(message = "Новый пароль не может быть пустым")
        @Size(min = 6, message = "Пароль должен содержать минимум 6 символов")
        String newPassword,
        
        @NotBlank(message = "Подтверждение пароля не может быть пустым")
        String confirmPassword
) {
}
