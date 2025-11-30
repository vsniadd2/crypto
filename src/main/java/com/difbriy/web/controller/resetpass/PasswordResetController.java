package com.difbriy.web.controller.resetpass;

import com.difbriy.web.dto.user.PasswordResetConfirmDto;
import com.difbriy.web.dto.user.PasswordResetRequestDto;
import com.difbriy.web.dto.user.PasswordResetResponseDto;
import com.difbriy.web.service.user.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/reset-password")
public class PasswordResetController {
    private final UserService userService;

    @PostMapping
    public ResponseEntity<PasswordResetResponseDto> requestPasswordReset(
            @RequestBody @Valid PasswordResetRequestDto dto
    ) {
        PasswordResetResponseDto response = userService.generatePasswordResetToken(dto.email());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/confirm")
    public ResponseEntity<PasswordResetResponseDto> confirmPasswordReset(
            @RequestBody @Valid PasswordResetConfirmDto dto
    ) {
        PasswordResetResponseDto response = userService.resetPassword(dto);
        return ResponseEntity.ok(response);
    }
}
