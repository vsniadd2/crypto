package com.difbriy.web.service.user;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

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

    public Optional<User> getPersonById(Long id) {
        return userRepository.findById(id);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
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

    @Transactional
    public User updateProfile(Long userId, String username, String email) {
        User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found"));
        validateUpdate(user, userId, username, email);
        user.setUsername(username);
        user.setEmail(email);
        return userRepository.save(user);
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
