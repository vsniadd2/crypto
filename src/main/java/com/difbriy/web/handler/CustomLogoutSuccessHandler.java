package com.difbriy.web.handler;
import com.difbriy.web.exception.custom.LogoutHandlerException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomLogoutSuccessHandler implements LogoutSuccessHandler {

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        try {
            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType("application/json");
            String body = "{\"message\": \"Logout successful\"}";
            response.getWriter().write(body);
        } catch (IOException e) {
            throw new LogoutHandlerException("Failed to write logout success response: " + e.getMessage());
        }
    }
}
