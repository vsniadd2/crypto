package com.difbriy.web.auth;

import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthenticationController {
    private final AuthenticationService authenticationService;

    @PostMapping("/registration")
    public CompletableFuture<ResponseEntity<AuthenticationResponse>> registration(@Valid @RequestBody RegistrationRequest request) {
        return authenticationService.registerAsync(request)
                .thenApply(response ->
                        ResponseEntity
                                .status(HttpStatus.OK)
                                .body(response)
                );
    }

    @PostMapping("/authenticate")
    public CompletableFuture<ResponseEntity<AuthenticationResponse>> authenticate(
            @Valid @RequestBody AuthenticationRequest authenticationRequest) {
        return authenticationService.authenticateAsync(authenticationRequest)
                .thenApply(response ->
                        ResponseEntity
                                .status(HttpStatus.OK)
                                .body(response)
                );
    }

    @PostMapping("/refresh-token")
    public CompletableFuture<ResponseEntity<AuthenticationResponse>> refreshToken(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader
    ) {
        return authenticationService.refreshTokenAsync(authHeader)
                .thenApply(response ->
                        ResponseEntity
                                .status(HttpStatus.OK)
                                .body(response)
                );
    }
}