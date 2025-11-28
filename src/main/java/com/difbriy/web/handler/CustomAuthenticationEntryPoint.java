package com.difbriy.web.handler;

import com.difbriy.web.exception.custom.AuthenticationEntryPointException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) {
        try {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            String body = String.format(
                    "{\"error\": \"unauthorized\", \"message\": \"%s\"}",
                    authException.getMessage()
            );
            response.getWriter().write(body);
        } catch (IOException e) {
            throw new AuthenticationEntryPointException("Failed to write authentication entry point response: " + e.getMessage());
        }
    }
}
