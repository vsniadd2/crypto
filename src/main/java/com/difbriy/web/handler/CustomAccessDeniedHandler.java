package com.difbriy.web.handler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) {
        try {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");

            String body = String.format(
                    "{\"error\": \"forbidden\", \"message\": \"%s\"}",
                    accessDeniedException.getMessage()
            );

            response.getWriter().write(body);
        } catch (IOException e) {
            throw new AccessDeniedException("Failed to write access denied response: " + e.getMessage());
        }
    }
}
