package com.difbriy.web.controller.resetpass;

import com.difbriy.web.dto.user.CreateResetTokenRequest;
import com.difbriy.web.dto.user.PasswordResetRequest;
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
public class ResetPasswordController {
    private final UserService userService;

    @PostMapping("/create-token")
    public ResponseEntity<Void> createResetToken(@Valid @RequestBody CreateResetTokenRequest createResetTokenRequest) {
        userService.createResetToken(createResetTokenRequest.email());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/confirm")
    public ResponseEntity<Void> resetPassword(@RequestBody PasswordResetRequest request) {
        userService.resetPassword(request.token(), request.password());
        return ResponseEntity.ok().build();
    }

}
