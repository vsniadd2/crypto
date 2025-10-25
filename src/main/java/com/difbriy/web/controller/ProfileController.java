package com.difbriy.web.controller;

import com.difbriy.web.dto.ProfileDto;
import com.difbriy.web.dto.UpdateDto;
import com.difbriy.web.dto.UpdateProfileDto;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import com.difbriy.web.entity.User;
import com.difbriy.web.service.user.UserService;
import com.difbriy.web.service.security.CustomUserDetailsService;
import com.difbriy.web.service.security.JwtService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
@Slf4j
public class ProfileController {
    private final UserService userService;
    private final CustomUserDetailsService customUserDetailsService;
    private final JwtService jwtService;

    @GetMapping
    public ResponseEntity<?> getProfile(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null)
            throw new AuthenticationCredentialsNotFoundException("User not authenticated");

        String email = userDetails.getUsername();
        log.info("Getting profile for email: {}", email);

        User user = userService.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

        return ResponseEntity.ok(ProfileDto.success(
                user.getId(),
                user.getUsername(),
                user.getEmail()
        ));
    }

    @PutMapping
    public ResponseEntity<?> updateProfile(@RequestBody UpdateDto request,
                                           @AuthenticationPrincipal UserDetails userDetails) {
        
        if (userDetails == null) {
            throw new AuthenticationCredentialsNotFoundException("User not authenticated");
        }

        String email = userDetails.getUsername();
        log.info("Extracted email from userDetails: {}", email);
        User user = userService.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        User updatedUser = userService.updateProfile(
                user.getId(),
                request.username(),
                user.getEmail()
        );


        UserDetails updatedUserDetails = customUserDetailsService.loadUserByUsername(user.getEmail());
        String newToken = jwtService.generateToken(updatedUserDetails);

        return ResponseEntity.ok(UpdateProfileDto.success(
                updatedUser.getId(),
                updatedUser.getUsername(),
                updatedUser.getEmail(),
                newToken
        ));
    }

}