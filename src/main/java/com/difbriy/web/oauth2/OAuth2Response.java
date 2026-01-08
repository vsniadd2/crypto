package com.difbriy.web.oauth2;

import com.difbriy.web.dto.user.UserDto;
import com.difbriy.web.token.TokenType;
import lombok.Builder;

@Builder
public record OAuth2Response(
        boolean success,
        String token,
        TokenType type,
        UserDto user
) {
}
