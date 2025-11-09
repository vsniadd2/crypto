package com.difbriy.web.dto.user;

import java.time.LocalDateTime;

public record ProfileDto(
        Long id,
        String username,
        String email,
        LocalDateTime dateTimeOfCreated
) {
    public static ProfileDto success(Long id, String username, String email,LocalDateTime dateTimeOfCreated) {
        return new ProfileDto(id, username, email,dateTimeOfCreated);
    }
}
