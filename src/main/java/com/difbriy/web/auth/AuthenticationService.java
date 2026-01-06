package com.difbriy.web.auth;

import java.util.concurrent.CompletableFuture;

public interface AuthenticationService {
    CompletableFuture<AuthenticationResponse> registerAsync(RegistrationRequest request);
    CompletableFuture<AuthenticationResponse> authenticateAsync(AuthenticationRequest request);
    AuthenticationResponse refreshToken(String authHeader);
    CompletableFuture<AuthenticationResponse> refreshTokenAsync(String authHeader);


}
