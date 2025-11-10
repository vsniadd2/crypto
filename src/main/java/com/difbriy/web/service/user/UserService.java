package com.difbriy.web.service.user;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import com.difbriy.web.dto.user.ProfileDto;
import com.difbriy.web.dto.user.UpdatedProfileResponseDto;
import com.difbriy.web.mapper.ProfileMapper;
import com.difbriy.web.mapper.UserMapper;
import com.difbriy.web.service.security.CustomUserDetailsService;
import com.difbriy.web.service.security.JwtService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.difbriy.web.entity.User;
import com.difbriy.web.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    private final UserRepository userRepository;
    private final Path avatarStoragePath = Paths.get("uploads/avatars");
    private final UserMapper mapper;
    private final CustomUserDetailsService customUserDetailsService;
    private final JwtService jwtService;
    private final ProfileMapper profileMapper;

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public ProfileDto getProfileByEmail(String email) {
        User user = findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
        return profileMapper.toDto(user);
    }

    //TODO(восстоновление паролья)
    public void createPasswordRestToken(String email) {
        Optional<User> person = userRepository.findByEmail(email);
        if (person.isPresent()) {
            String token = UUID.randomUUID().toString();
            person.get().setResetToken(token);
            person.get().setResetTokenExpiry(LocalDateTime.now().plusHours(1));
            userRepository.save(person.get());
        } else {
            log.warn("User not found with email: {}", email);
        }
    }

    //TODO
    public User findByResetToken(String token) {
        return userRepository.findByResetToken(token).orElseThrow(() -> new RuntimeException("Токен не найден"));
    }

    @Transactional()
    public UpdatedProfileResponseDto updateProfile(Long userId, String username, String email) {
        User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found"));
        validateUpdate(user, userId, username, email);
        user.setUsername(username);
        user.setEmail(email);

        User updatedUser = userRepository.save(user);
        var updatedUserDetails = customUserDetailsService.loadUserByUsername(updatedUser.getEmail());
        String token = jwtService.generateToken(updatedUserDetails);

        return mapper.toUpdatedProfileDto(user, token);
    }

    @Async
    public CompletableFuture<?> updateProfileAsync(Long userId, String username, String email) {
        return CompletableFuture.supplyAsync(() -> updateProfile(userId, username, email));
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
