package com.difbriy.web.controller;

import com.difbriy.web.dto.user.PasswordResetRequestDto;
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

//    @PostMapping
//    public ResponseEntity<?> resetPassword(@RequestBody @Valid PasswordResetRequestDto dto){
//
//    }

}
