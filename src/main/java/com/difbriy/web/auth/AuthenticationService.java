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
    private final MailService mailService;

    @Transactional
    public AuthenticationResponse register(RegistrationRequest request) {
        validateRegister(request);
        var user = userMapper.toEntity(request);
        var savedUser = userRepository.save(user);

        UserDetails userDetails = customUserDetailsService.loadUserByUsername(request.email());
        var jwtToken = jwtService.generateToken(userDetails);
        var refreshToken = jwtService.generateRefreshToken(userDetails);
        savedUserToken(savedUser, jwtToken);

        UserDto userDto = userMapper.toDto(user);

        //комментарий над самим методом
        runAfterCommit(
                () -> mailService.sendWelcomeEmailAsync(request.email())
                        .exceptionally(ex -> {
                            log.error("Async email sending failed for {}", request.email(), ex);
                            return null;
                        })
        );
        return AuthenticationResponse.registration(
                jwtToken,
                refreshToken,
                userDto
        );
    }

    @Async
    public CompletableFuture<AuthenticationResponse> registerAsync(RegistrationRequest request) {
        return CompletableFuture.supplyAsync(() -> register(request));
    }

    @Transactional
    public AuthenticationResponse authenticate(AuthenticationRequest authenticationRequest) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(authenticationRequest.email(), authenticationRequest.password())
        );

        User user = userRepository.findByEmail(authenticationRequest.email())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        UserDetails userDetails = customUserDetailsService.loadUserByUsername(authenticationRequest.email());
        var jwtToken = jwtService.generateToken(userDetails);
        var refreshToken = jwtService.generateRefreshToken(userDetails);
        revokeAllUserToken(user);
        savedUserToken(user, jwtToken);
        savedUserRefreshToken(user, refreshToken);

        UserDto userDto = userMapper.toDto(user);
        return AuthenticationResponse.login(
                jwtToken,
                refreshToken,
                userDto
        );
    }

    @Async
    public CompletableFuture<AuthenticationResponse> authenticateAsync(AuthenticationRequest authenticationRequest) {
        return CompletableFuture.supplyAsync(() -> authenticate(authenticationRequest));
    }


    @Transactional
    public AuthenticationResponse refreshToken(String authHeader) {
        validateRefreshToken(authHeader);

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

        UserDetails userDetails = customUserDetailsService.loadUserByUsername(email);

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

    public CompletableFuture<AuthenticationResponse> refreshTokenAsync(String authHeader) {
        return CompletableFuture.supplyAsync(() -> refreshToken(authHeader));
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

    /**
     * Отправка письма о регистрации пользователя без вреда производительности метода регистрации
     *
     **/
    private void runAfterCommit(Runnable runnable) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                runnable.run();
            }
        });
    }
}
