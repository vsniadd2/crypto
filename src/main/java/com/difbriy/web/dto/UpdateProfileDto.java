package com.difbriy.web.dto;


public record UpdateProfileDto(
        String message,
        User user,
        String token

) {
    public static UpdateProfileDto success(Long id, String username, String email) {
        String message = "Profile updated successfully";
        User user = new User(id, username, email);
        return new UpdateProfileDto(message, user, null);
    }
    
    public static UpdateProfileDto success(Long id, String username, String email, String token) {
        String message = "Profile updated successfully";
        User user = new User(id, username, email);
        return new UpdateProfileDto(message, user, token);
    }

    public record User(
            Long id,
            String username,
            String email) {
    }
}
