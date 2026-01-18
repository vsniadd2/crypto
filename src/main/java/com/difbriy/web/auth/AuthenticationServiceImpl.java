package com.difbriy.web.auth;

import com.difbriy.web.dto.user.UserDto;
import com.difbriy.web.entity.User;
import com.difbriy.web.mapper.UserMapper;
import com.difbriy.web.repository.UserRepository;
import com.difbriy.web.service.mail.MailService;
import com.difbriy.web.service.security.CustomUserDetailsService;
import com.difbriy.web.service.security.JwtService;
import com.difbriy.web.token.TokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationServiceImpl implements AuthenticationService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CustomUserDetailsService customUserDetailsService;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserMapper userMapper;
    private final TokenRepository tokenRepository;
    private final MailService mailServiceImpl;
    private final TransactionTemplate transactionTemplate;

    @Async("taskExecutor")
    public CompletableFuture<AuthenticationResponse> registerAsync(RegistrationRequest request) {
        return CompletableFuture.supplyAsync(() ->
                transactionTemplate.execute(status -> {
                    try {
                        validateRegister(request);

                        User user = userMapper.toEntity(request, passwordEncoder);
                        user = userRepository.save(user);

                        UserDetails details = jwtService.loadUserDetails(request.email());
                        String access = jwtService.generateToken(details);
                        String refresh = jwtService.generateRefreshToken(details);
                        jwtService.savedUserToken(user, access);

                        AuthenticationResponse resp = AuthenticationResponse.registration(
                                access, refresh, userMapper.toDto(user)
                        );

                        return resp;
                    } catch (Exception e) {
                        status.setRollbackOnly();
                        throw e;
                    }
                })
        ).whenComplete((resp, ex) -> {
            if (ex == null && resp != null) {
                sendWelcomeEmailAfterCommit(request.email());
            } else {
                log.warn("Registration succeeded but email not sent due to exception: {}", ex);
            }
        });
    }


    @Async("taskExecutor")
    public CompletableFuture<AuthenticationResponse> authenticateAsync(AuthenticationRequest request) {
        return CompletableFuture.supplyAsync(() ->
                        transactionTemplate.execute(status -> {
                            try {
                                authenticationManager.authenticate(
                                        new UsernamePasswordAuthenticationToken(request.email(), request.password())
                                );

                                UserDetails userDetails = jwtService.loadUserDetails(request.email());

                                String jwtToken = jwtService.generateToken(userDetails);
                                String refreshToken = jwtService.generateRefreshToken(userDetails);

                                User user = userRepository.findByEmail(request.email())
                                        .orElseThrow(() -> new UsernameNotFoundException(
                                                String.format("User not found with email: %s", request.email())
                                        ));

                                jwtService.revokeAllUserToken(user);
                                jwtService.savedUserToken(user, jwtToken);
                                jwtService.savedUserRefreshToken(user, refreshToken);

                                AuthenticationResponse response = AuthenticationResponse.login(
                                        jwtToken,
                                        refreshToken,
                                        userMapper.toDto(user)
                                );


                                return response;
                            } catch (Exception e) {
                                status.setRollbackOnly();
                                throw e;
                            }
                        })
        ).exceptionally(ex -> {
            log.error("Async authentication failed for email: {}", request.email(), ex);
            throw new CompletionException(ex);
        });
    }

    @Transactional
    public AuthenticationResponse refreshToken(String authHeader) {
        validateRefreshToken(authHeader);
        log.info("Refreshing token...");

        String refreshToken = authHeader.substring(7);
        String email = jwtService.getUsername(refreshToken);

        if (email == null || email.isEmpty())
            throw new IllegalArgumentException("Invalid refresh token");

        String tokenType = jwtService.extractClaim(refreshToken, claims -> claims.get("typ", String.class));
        if (!"refresh_token".equals(tokenType)) {
            throw new IllegalArgumentException("Invalid token type for refresh");
        }

        var user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        UserDetails userDetails = jwtService.loadUserDetails(email);

        if (!jwtService.isTokenValid(refreshToken, userDetails))
            throw new IllegalArgumentException("Refresh token is not valid");

        var storedRefreshToken = tokenRepository.findByToken(refreshToken);
        if (storedRefreshToken.isEmpty() || storedRefreshToken.get().isExpired() || storedRefreshToken.get().isRevoked()) {
            throw new IllegalArgumentException("Refresh token is not valid or has been revoked");
        }

        String newAccessToken = jwtService.generateToken(userDetails);
        String newRefreshToken = jwtService.generateRefreshToken(userDetails);
        jwtService.revokeAllUserToken(user);
        jwtService.savedUserToken(user, newAccessToken);
        jwtService.savedUserRefreshToken(user, newRefreshToken);

        UserDto userDto = userMapper.toDto(user);
        return AuthenticationResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .success(true)
                .user(userDto)
                .build();
    }

    @Async("taskExecutor")
    public CompletableFuture<AuthenticationResponse> refreshTokenAsync(String authHeader) {
        return CompletableFuture.completedFuture(refreshToken(authHeader));
    }

    private void validateRegister(RegistrationRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Email already exists");
        }
        if (userRepository.existsByUsername(request.username())) {
            throw new IllegalArgumentException("Username already exists");
        }
    }

    private void validateRefreshToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer "))
            throw new IllegalArgumentException("Missing or invalid Authorization header");
    }

    private void sendWelcomeEmailAfterCommit(String email) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            runAfterCommit(
                    () -> mailServiceImpl.sendWelcomeEmailAsync(email)
                            .exceptionally(ex -> {
                                log.error("Async email sending failed for {}", email, ex);
                                return null;
                            })
            );
        } else {
            mailServiceImpl.sendWelcomeEmailAsync(email);
        }
    }

    private void runAfterCommit(Runnable runnable) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                runnable.run();
            }
        });
    }
}

