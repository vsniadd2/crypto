package com.difbriy.web.oauth2.handler;

import com.difbriy.web.config.CustomUserDetails;
import com.difbriy.web.dto.user.UserDto;
import com.difbriy.web.entity.User;
import com.difbriy.web.oauth2.OAuth2Response;
import com.difbriy.web.repository.UserRepository;
import com.difbriy.web.roles.Role;
import com.difbriy.web.service.security.JwtService;
import com.difbriy.web.token.TokenType;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.Optional;;

@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    private final UserRepository userRepository;
    private final JwtService jwtService;
    @Lazy
    private final PasswordEncoder passwordEncoder;
    private final ObjectMapper objectMapper;

    @Override
    public void onAuthenticationSuccess(
            final HttpServletRequest request,
            final HttpServletResponse response,
            final Authentication authentication
    ) throws IOException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String githubId = oAuth2User.getAttribute("id").toString();
        String username = oAuth2User.getAttribute("login");

        String email = Optional.ofNullable(oAuth2User.getAttribute("email"))
                .orElseGet(() -> username + "@github.com").toString();

        User user = userRepository.findByGithubId(githubId)
                .orElseGet(() -> createNewUser(githubId, username, email));

        String jwtToken = jwtService.generateToken(new CustomUserDetails(user));
        response.setContentType("application/json; charset=UTF-8");
        response.setStatus(200);

        UserDto userResponse = UserDto.builder()
                .id(user.getId())
                .username(username)
                .email(email)
                .build();

        OAuth2Response responseDto = OAuth2Response.builder()
                .success(true)
                .token(jwtToken)
                .type(TokenType.ACCESS)
                .user(userResponse)
                .build();


        objectMapper.writeValue(response.getWriter(), responseDto);
    }

    private User createNewUser(final String githubId, final String username, final String email) {
        var user = User.builder()
                .username(username)
                .email(email)
                .password(passwordEncoder.encode(githubId.concat(username)))
                .roles(List.of(Role.ROLE_USER))
                .isActive(true)
                .githubId(githubId)
                .build();
        return userRepository.save(user);
    }
}
