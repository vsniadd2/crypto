package com.difbriy.web.auth;

import com.difbriy.web.dto.user.UserDto;
import com.difbriy.web.entity.User;
import com.difbriy.web.mapper.UserMapper;
import com.difbriy.web.repository.UserRepository;
import com.difbriy.web.service.mail.MailService;
import com.difbriy.web.service.security.CustomUserDetailsService;
import com.difbriy.web.service.security.JwtService;
import com.difbriy.web.token.Token;
import com.difbriy.web.token.TokenRepository;
import com.difbriy.web.token.TokenType;
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

import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CustomUserDetailsService customUserDetailsService;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserMapper userMapper;
    private final TokenRepository tokenRepository;
    private final MailService mailServiceImpl;

    @Transactional
    public AuthenticationResponse register(RegistrationRequest request) {
        validateRegister(request);

        var savedUser = userRepository.save(userMapper.toEntity(request, passwordEncoder));

        UserDetails userDetails = loadUserDetails(request.email());
        var jwtToken = jwtService.generateToken(userDetails);
        var refreshToken = jwtService.generateRefreshToken(userDetails);
        savedUserToken(savedUser, jwtToken);

        sendWelcomeEmailAfterCommit(request.email());

        return AuthenticationResponse.registration(
                jwtToken,
                refreshToken,
                userMapper.toDto(savedUser)
        );
    }

    @Async("taskExecutor")
    public CompletableFuture<AuthenticationResponse> registerAsync(RegistrationRequest request) {
        return CompletableFuture.completedFuture(register(request));
    }

    @Transactional
    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        UserDetails userDetails = loadUserDetails(request.email());
        var jwtToken = jwtService.generateToken(userDetails);
        var refreshToken = jwtService.generateRefreshToken(userDetails);

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() ->
                        new UsernameNotFoundException(String
                                .format("User not found with email: %s", request.email()))
                );

        revokeAllUserToken(user);
        savedUserToken(user, jwtToken);
        savedUserRefreshToken(user, refreshToken);

        return AuthenticationResponse.login(
                jwtToken,
                refreshToken,
                userMapper.toDto(user)
        );
    }

    @Async("taskExecutor")
    public CompletableFuture<AuthenticationResponse> authenticateAsync(AuthenticationRequest request) {
        return CompletableFuture.completedFuture(authenticate(request));
    }

    @Transactional
    public AuthenticationResponse refreshToken(String authHeader) {
        validateRefreshToken(authHeader);
        log.info("Refreshing token...");

        String refreshToken = authHeader.substring(7);
        String email = jwtService.getUsername(refreshToken);

        if (email == null)
            throw new IllegalArgumentException("Invalid refresh token");

        String tokenType = jwtService.extractClaim(refreshToken, claims -> claims.get("typ", String.class));
        if (!"refresh_token".equals(tokenType)) {
            throw new IllegalArgumentException("Invalid token type for refresh");
        }

        var user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        UserDetails userDetails = loadUserDetails(email);

        if (!jwtService.isTokenValid(refreshToken, userDetails))
            throw new IllegalArgumentException("Refresh token is not valid");

        var storedRefreshToken = tokenRepository.findByToken(refreshToken);
        if (storedRefreshToken.isEmpty() || storedRefreshToken.get().isExpired() || storedRefreshToken.get().isRevoked()) {
            throw new IllegalArgumentException("Refresh token is not valid or has been revoked");
        }

        String newAccessToken = jwtService.generateToken(userDetails);
        String newRefreshToken = jwtService.generateRefreshToken(userDetails);
        revokeAllUserToken(user);
        savedUserToken(user, newAccessToken);
        savedUserRefreshToken(user, newRefreshToken);

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

    private UserDetails loadUserDetails(final String email) {
        return customUserDetailsService.loadUserByUsername(email);
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

    private void revokeAllUserToken(User user) {
        var validUserTokens = tokenRepository.findAllValidTokensByUser(user.getId().intValue());
        if (validUserTokens.isEmpty())
            return;

        validUserTokens.forEach(t -> {
            t.setExpired(true);
            t.setRevoked(true);
        });

        tokenRepository.saveAll(validUserTokens);
    }

    private void savedUserToken(User user, String jwtToken) {
        var token = Token.builder()
                .user(user)
                .token(jwtToken)
                .tokenType(TokenType.ACCESS)
                .expired(false)
                .revoked(false)
                .build();
        tokenRepository.save(token);
    }

    private void savedUserRefreshToken(User user, String refreshToken) {
        var token = Token.builder()
                .user(user)
                .token(refreshToken)
                .tokenType(TokenType.REFRESH)
                .expired(false)
                .revoked(false)
                .build();
        tokenRepository.save(token);
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

    private void runAfterCommit(Runnable runnable) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                runnable.run();
            }
        });
    }
}

