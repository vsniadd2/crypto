package com.difbriy.web.admin.service;

import com.difbriy.web.dto.admin.UserAdminDto;
import com.difbriy.web.entity.User;
import com.difbriy.web.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserAdminService {
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public Page<UserAdminDto> getAllUsersWithPagination(int page, int size) {
        log.info("Fetching users with pagination: page={}, size={}", page, size);
        Pageable pageable = PageRequest.of(page, size);

        Page<User> users = userRepository.findAll(pageable);

        log.info("Fetched {} users on page {} of {} (total elements={})",
                users.getNumberOfElements(),
                users.getNumber(),
                users.getTotalPages(),
                users.getTotalElements()
        );
        return users.map(user -> UserAdminDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .roles(user.getRoles())
                .timestamp(user.getDateTimeOfCreated())
                .isActive(user.isActive())
                .build());
    }

    public UserAdminDto blockUser(Long userId) {
        log.info("Blocking user with id: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("User with id {} not found", userId);
                    return new IllegalArgumentException("User with id %d not found".formatted(userId));
                });

        if (!user.isActive()) {
            log.warn("User with id {} is already blocked", userId);
            throw new IllegalArgumentException("User with id " + userId + " is already blocked");
        }

        user.setActive(false);
        User savedUser = userRepository.save(user);

        log.info("User with id {} has been blocked successfully", userId);

        return UserAdminDto.builder()
                .username(savedUser.getUsername())
                .email(savedUser.getEmail())
                .roles(savedUser.getRoles())
                .timestamp(savedUser.getDateTimeOfCreated())
                .isActive(savedUser.isActive())
                .build();
    }

    public UserAdminDto unblockUser(Long userId) {
        log.info("Unblocking user with id: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("User with id {} not found", userId);
                    return new IllegalArgumentException("User with id " + userId + " not found");
                });

        if (user.isActive()) {
            log.warn("User with id {} is already active", userId);
            throw new IllegalArgumentException("User with id " + userId + " is already active");
        }

        user.setActive(true);
        User savedUser = userRepository.save(user);

        log.info("User with id {} has been unblocked successfully", userId);

        return UserAdminDto.builder()
                .username(savedUser.getUsername())
                .email(savedUser.getEmail())
                .roles(savedUser.getRoles())
                .timestamp(savedUser.getDateTimeOfCreated())
                .isActive(savedUser.isActive())
                .build();
    }

    @Transactional(readOnly = true)
    public long countUsers() {
        long count = userRepository.count();
        log.info("Total users in system: {}", count);
        return count;
    }
}
