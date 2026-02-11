package com.difbriy.web.auth;

import com.difbriy.web.dto.event.UserRegisteredEvent;
import com.difbriy.web.dto.user.UserDto;
import com.difbriy.web.entity.User;
import com.difbriy.web.mapper.UserMapper;
import com.difbriy.web.repository.UserRepository;
import com.difbriy.web.service.mail.MailService;
import com.difbriy.web.service.security.CustomUserDetailsService;
import com.difbriy.web.service.security.JwtService;
import com.difbriy.web.token.TokenRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
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
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class AuthenticationServiceImpl implements AuthenticationService {
    UserRepository userRepository;
    PasswordEncoder passwordEncoder;
    CustomUserDetailsService customUserDetailsService;
    AuthenticationManager authenticationManager;
    JwtService jwtService;
    UserMapper userMapper;
    TokenRepository tokenRepository;

    TransactionTemplate transactionTemplate;
    ApplicationEventPublisher applicationEventPublisher;

    @Async("taskExecutor")
    @Override
    public CompletableFuture<AuthenticationResponse> registerAsync(RegistrationRequest request) {
        return CompletableFuture.supplyAsync(() ->
                transactionTemplate.execute(status -> {
                    validateRegister(request);

                    User user = userMapper.toEntity(request, passwordEncoder);
                    user = userRepository.save(user);

                    UserDetails details = jwtService.loadUserDetails(request.email());
                    String access = jwtService.generateToken(details);
                    String refresh = jwtService.generateRefreshToken(details);
                    jwtService.savedUserToken(user, access);

                    applicationEventPublisher.publishEvent(new UserRegisteredEvent(user.getEmail()));

                    return AuthenticationResponse.registration(
                            access, refresh, userMapper.toDto(user)
                    );
                })
        );
    }


    @Async("taskExecutor")
    @Override
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

    @Async("taskExecutor")
    @Override
    public CompletableFuture<AuthenticationResponse> refreshTokenAsync(String authHeader) {
        return CompletableFuture.supplyAsync(() -> {
            validateRefreshToken(authHeader);
            String refreshToken = authHeader.substring(7);
            String email = jwtService.getUsername(refreshToken);

            if (email == null || email.isEmpty())
                throw new IllegalArgumentException("Invalid refresh token");


            return transactionTemplate.execute(status -> {
                var user = userRepository.findByEmail(email)
                        .orElseThrow(() -> new UsernameNotFoundException("User not found"));

                UserDetails userDetails = jwtService.loadUserDetails(email);

                var storedToken = tokenRepository.findByToken(refreshToken)
                        .filter(t -> !t.isExpired() && !t.isRevoked())
                        .orElseThrow(() -> new IllegalArgumentException("Token revoked or expired"));

                String newAccess = jwtService.generateToken(userDetails);
                String newRefresh = jwtService.generateRefreshToken(userDetails);

                jwtService.revokeAllUserToken(user);
                jwtService.savedUserToken(user, newAccess);
                jwtService.savedUserRefreshToken(user, newRefresh);

                return AuthenticationResponse.builder()
                        .accessToken(newAccess)
                        .refreshToken(newRefresh)
                        .success(true)
                        .user(userMapper.toDto(user))
                        .build();
            });
        });
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

}

