package com.difbriy.web.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CreateResetTokenRequest(
    @Email
    @NotBlank
    String email
) {
}
