package com.difbriy.web.handler;

import com.difbriy.web.exception.custom.LogoutHandlerException;
import com.difbriy.web.token.TokenRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class CustomLogoutHandler implements LogoutHandler{
    private final TokenRepository tokenRepository;

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        try {
            final String authHeader = request.getHeader("Authorization");
            final String jwt;
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return;
            }
            jwt = authHeader.substring(7);

            var storedToken = tokenRepository.findByToken(jwt)
                    .orElse(null);
            if (storedToken != null) {
                storedToken.setExpired(true);
                storedToken.setRevoked(true);
                tokenRepository.save(storedToken);
                SecurityContextHolder.clearContext();
            }
        } catch (Exception e) {
            throw new LogoutHandlerException("Failed to process logout: " + e.getMessage());
        }
    }
}
