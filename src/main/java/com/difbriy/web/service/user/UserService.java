package com.difbriy.web.service.user;

import com.difbriy.web.dto.user.ProfileDto;
import com.difbriy.web.dto.user.UpdatedProfileResponseDto;
import com.difbriy.web.entity.User;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface UserService {
    Optional<User> findByEmail(String email);

    ProfileDto getProfileByEmail(String email);

    User findByResetToken(String token);

    CompletableFuture<UpdatedProfileResponseDto> updateProfile(Long userId, String username, String email);


    void createResetToken(String email);
    void resetPassword(String token, String newPassword);
}
