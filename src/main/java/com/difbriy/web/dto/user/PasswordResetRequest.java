package com.difbriy.web.dto.user;

public record PasswordResetRequest(
    String token,
    String password
) {
}
