package com.difbriy.web.dto;

public record ProfileDto(
        Long id,
        String username,
        String email
) {
    public static ProfileDto success(Long id, String username, String email) {
        return new ProfileDto(id, username, email);
    }
}
