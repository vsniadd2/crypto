package com.difbriy.web.service.user;

import com.difbriy.web.dto.user.PasswordResetConfirmDto;
import com.difbriy.web.dto.user.PasswordResetResponseDto;
import com.difbriy.web.dto.user.ProfileDto;
import com.difbriy.web.dto.user.UpdatedProfileResponseDto;
import com.difbriy.web.entity.User;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface UserService {
    Optional<User> findByEmail(String email);

    ProfileDto getProfileByEmail(String email);

    CompletableFuture<PasswordResetResponseDto> generatePasswordResetToken(String email);

    CompletableFuture<PasswordResetResponseDto> resetPassword(PasswordResetConfirmDto confirmDto);

    User findByResetToken(String token);

    CompletableFuture<UpdatedProfileResponseDto> updateProfile(Long userId, String username, String email);
}
