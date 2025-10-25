package com.difbriy.web.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AuthenticationRequest(
        @NotBlank
        @Email
        String email,
        @Size(min = 8)
        String password

) {
}
