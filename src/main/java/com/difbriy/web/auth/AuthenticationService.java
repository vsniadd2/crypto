package com.difbriy.web.auth;

import java.util.concurrent.CompletableFuture;

public interface AuthenticationService {
    CompletableFuture<AuthenticationResponse> registerAsync(RegistrationRequest request);
    CompletableFuture<AuthenticationResponse> authenticateAsync(AuthenticationRequest request);
    CompletableFuture<AuthenticationResponse> refreshTokenAsync(String authHeader);


}
