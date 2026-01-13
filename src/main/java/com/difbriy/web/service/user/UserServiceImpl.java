package com.difbriy.web.service.user;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import com.difbriy.web.dto.user.ProfileDto;
import com.difbriy.web.dto.user.UpdatedProfileResponseDto;
import com.difbriy.web.exception.custom.InvalidResetTokenException;
import com.difbriy.web.mapper.ProfileMapper;
import com.difbriy.web.mapper.UserMapper;
import com.difbriy.web.service.mail.MailService;
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
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final Path avatarStoragePath = Paths.get("uploads/avatars");
    private final UserMapper userMapper;
    private final CustomUserDetailsService customUserDetailsService;
    private final JwtService jwtService;
    private final ProfileMapper profileMapper;
    private final PasswordEncoder passwordEncoder;
    private final Executor executor;
    private final MailService mailService;

    @Value("${user-service.reset-password}")
    private String RESET_PASSWORD_URL;

    @Transactional(readOnly = true)
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);

    }

    @Transactional(readOnly = true)
    @Override
    public ProfileDto getProfileByEmail(String email) {
        User user = loadUserByEmail(email);
        return profileMapper.toDto(user);
    }

    @Transactional(readOnly = true)
    @Override
    public User findByResetToken(String token) {
        return userRepository.findByResetToken(token)
                .orElseThrow(() -> new InvalidResetTokenException(String.format("Token: %s not found", token)));
    }

    @Transactional
    @Override
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

    @Override
    public void createResetToken(String email) {
        User user = loadUserByEmail(email);
        String token = UUID.randomUUID().toString();
        user.setResetToken(token);
        user.setResetTokenExpiry(LocalDateTime.now().plusMinutes(15));
        userRepository.save(user);

        String resetLink = RESET_PASSWORD_URL + token;
        mailService.sendPasswordResetEmail(user.getEmail(), resetLink);
    }

    @Override
    public void resetPassword(String token, String newPassword) {
        User user = userRepository.findByResetToken(token)
                .orElseThrow(() -> new InvalidResetTokenException(String.format("Token: %s not found", token)));
        if (LocalDateTime.now().isAfter(user.getResetTokenExpiry()))
            throw new InvalidResetTokenException(String.format("Token: %s is expired", token));

        if (newPassword == null || newPassword.length() < 8) {
            throw new IllegalArgumentException("New password must be at least 8 characters long");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
    }

    private void validateUpdate(User user, Long userId, String username, String email) {
        if (!user.getEmail().equals(email) && userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already exists");
        }
        if (!user.getUsername().equals(username) && userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Username already exists");
        }
    }

    private User loadUserByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(() ->
                new UsernameNotFoundException(String.format("User not found with email: %s", email)));
    }
}
