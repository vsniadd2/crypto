package com.difbriy.web.controller.profile;

import com.difbriy.web.dto.user.ProfileDto;
import com.difbriy.web.dto.user.UpdateProfileRequestDto;
import com.difbriy.web.dto.user.UpdatedProfileResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import com.difbriy.web.entity.User;
import com.difbriy.web.service.user.UserService;
import com.difbriy.web.service.security.CustomUserDetailsService;
import com.difbriy.web.service.security.JwtService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
@Slf4j
public class ProfileController {
    private final UserService userService;
    private final CustomUserDetailsService customUserDetailsService;
    private final JwtService jwtService;

    @PreAuthorize("isAuthenticated()")
    @GetMapping
    public ResponseEntity<ProfileDto> getProfile(@AuthenticationPrincipal UserDetails userDetails) {

        String email = extractEmail(userDetails);
        log.info("Getting profile for email: {}", email);

        ProfileDto dto = userService.getProfileByEmail(email);

        return ResponseEntity.ok(dto);
    }

    @PreAuthorize("isAuthenticated()")
    @PutMapping
    public CompletableFuture<ResponseEntity<UpdatedProfileResponseDto>> updateProfile(@RequestBody UpdateProfileRequestDto request,
                                                                                      @AuthenticationPrincipal UserDetails userDetails) {
        String email = extractEmail(userDetails);
        log.info("Extracted email from userDetails: {}", email);
        User user = userService.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return userService.updateProfileAsync(user.getId(), user.getUsername(), user.getEmail())
                .thenApply(response ->
                        ResponseEntity
                                .status(HttpStatus.OK)
                                .body(response)
                );
    }

    private String extractEmail(UserDetails userDetails){
        return userDetails.getUsername();
    }

}