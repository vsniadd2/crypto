package com.difbriy.web.controller.resetpass;

import com.difbriy.web.dto.user.PasswordResetConfirmDto;
import com.difbriy.web.dto.user.PasswordResetRequestDto;
import com.difbriy.web.dto.user.PasswordResetResponseDto;
import com.difbriy.web.service.user.UserService;
import com.difbriy.web.service.user.UserServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/reset-password")
public class PasswordResetController {
    private final UserService userService;

    @PostMapping
    public CompletableFuture<ResponseEntity<PasswordResetResponseDto>> requestPasswordReset(
            @RequestBody @Valid PasswordResetRequestDto dto
    ) {
        return userService.generatePasswordResetToken(dto.email())
                .thenApply(ResponseEntity::ok);
    }

    @PostMapping("/confirm")
    public CompletableFuture<ResponseEntity<PasswordResetResponseDto>> confirmPasswordReset(
            @RequestBody @Valid PasswordResetConfirmDto dto
    ) {
        return userService.resetPassword(dto)
                .thenApply(ResponseEntity::ok);
    }
}
