package com.difbriy.web.service.user;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import com.difbriy.web.dto.user.PasswordResetConfirmDto;
import com.difbriy.web.dto.user.PasswordResetResponseDto;
import com.difbriy.web.dto.user.ProfileDto;
import com.difbriy.web.dto.user.UpdatedProfileResponseDto;
import com.difbriy.web.exception.custom.InvalidResetTokenException;
import com.difbriy.web.mapper.ProfileMapper;
import com.difbriy.web.mapper.UserMapper;
import com.difbriy.web.service.security.CustomUserDetailsService;
import com.difbriy.web.service.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.difbriy.web.entity.User;
import com.difbriy.web.repository.UserRepository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    private final UserRepository userRepository;
    private final Path avatarStoragePath = Paths.get("uploads/avatars");
    private final UserMapper userMapper;
    private final CustomUserDetailsService customUserDetailsService;
    private final JwtService jwtService;
    private final ProfileMapper profileMapper;
    private final PasswordEncoder passwordEncoder;
    private final Executor executor;

    @Value("${user-service.reset-password}")
    private String RESET_PASSWORD_URL;

    @Transactional(readOnly = true)
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);

    }

    @Transactional(readOnly = true)
    public ProfileDto getProfileByEmail(String email) {
        User user = findByEmail(email).orElseThrow(
                () -> new UsernameNotFoundException(String.format("User with email %s not found", email))
        );
        return profileMapper.toDto(user);
    }

    @Transactional
    public CompletableFuture<PasswordResetResponseDto> generatePasswordResetToken(String email) {
        return CompletableFuture.supplyAsync(() -> {
            String token = UUID.randomUUID().toString();
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
            user.setResetToken(token);
            user.setResetTokenExpiry(LocalDateTime.now().plusHours(1));
            userRepository.save(user);

            String resetUrl = RESET_PASSWORD_URL + token;

            return PasswordResetResponseDto.builder()
                    .message("success")
                    .resetToken(token)
                    .resetUrl(resetUrl)
                    .expiresAt(user.getResetTokenExpiry())
                    .success(true)
                    .build();
        }, executor);
    }

    @Transactional
    public CompletableFuture<PasswordResetResponseDto> resetPassword(PasswordResetConfirmDto confirmDto) {
        return CompletableFuture.supplyAsync(() -> {
            User user = validateToken(confirmDto);

            user.setPassword(passwordEncoder.encode(confirmDto.newPassword()));
            user.setResetToken(null);
            user.setResetTokenExpiry(null);
            userRepository.save(user);

            log.info("Password successfully reset for user: {}", user.getEmail());
            return PasswordResetResponseDto.passwordChanged();
        }, executor);
    }

    @Transactional(readOnly = true)
    public User findByResetToken(String token) {
        return userRepository.findByResetToken(token)
                .orElseThrow(() -> new InvalidResetTokenException(String.format("Token: $s not found", token)));
    }

    @Transactional
    public CompletableFuture<UpdatedProfileResponseDto> updateProfile(Long userId, String username, String email) {
        return CompletableFuture.supplyAsync(() -> {
            User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found"));
            validateUpdate(user, userId, username, email);
            user.setUsername(username);
            user.setEmail(email);

            User updatedUser = userRepository.save(user);
            var updatedUserDetails = customUserDetailsService.loadUserByUsername(updatedUser.getEmail());
            String token = jwtService.generateToken(updatedUserDetails);

            return userMapper.toUpdatedProfileDto(user, token);
        }, executor);
    }


    private User validateToken(PasswordResetConfirmDto dto) {
        if (dto.token() == null || dto.token().trim().isEmpty()) {
            throw new InvalidResetTokenException("Token cannot be empty");
        }

        User user = userRepository.findByResetToken(dto.token())
                .orElseThrow(() -> new InvalidResetTokenException("Invalid reset token"));

        if (user.getResetTokenExpiry() == null || user.getResetTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new InvalidResetTokenException("The token has expired");
        }

        if (!dto.newPassword().equals(dto.confirmPassword())) {
            throw new InvalidResetTokenException("The passwords don't match");
        }

        return user;
    }

    private void validateUpdate(User user, Long userId, String username, String email) {
        if (!user.getEmail().equals(email) && userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already exists");
        }
        if (!user.getUsername().equals(username) && userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Username already exists");
        }
    }
}
