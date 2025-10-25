package com.difbriy.web.dto;

public record OAuth2Dto(
        boolean success,
        String message,
        String token,
        User user
) {
    public static OAuth2Dto success(String token, Long id, String username, String email) {
        User userDto = new User(id, username, email);
        return new OAuth2Dto(true, "Login successful", token, userDto);
    }


    public record User(
            Long id,
            String username,
            String email
    ) {
    }
}
