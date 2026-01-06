package com.difbriy.web.auth;

import com.difbriy.web.dto.user.UserDto;
import com.difbriy.web.entity.User;
import com.difbriy.web.mapper.UserMapper;
import com.difbriy.web.repository.UserRepository;
import com.difbriy.web.service.mail.MailServiceImpl;
import com.difbriy.web.service.security.CustomUserDetailsService;
import com.difbriy.web.service.security.JwtService;
import com.difbriy.web.token.Token;
import com.difbriy.web.token.TokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private CustomUserDetailsService customUserDetailsService;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private JwtService jwtService;
    @Mock
    private UserMapper userMapper;
    @Mock
    private TokenRepository tokenRepository;
    @Mock
    private MailServiceImpl mailServiceImpl;

    private AuthenticationService authenticationService;

    @BeforeEach
    void setUp() {
        authenticationService = new AuthenticationServiceImpl(
                userRepository,
                passwordEncoder,
                customUserDetailsService,
                authenticationManager,
                jwtService,
                userMapper,
                tokenRepository,
                mailServiceImpl
        );
    }

    @Test
    void register_shouldCreateUserAndReturnTokens() throws ExecutionException, InterruptedException {
        RegistrationRequest request = new RegistrationRequest("johnDoe", "john@example.com", "password123");
        User mappedUser = User.builder().username("johnDoe").email("john@example.com").build();
        User savedUser = User.builder().id(1L).username("johnDoe").email("john@example.com").build();
        UserDto userDto = UserDto.builder().id(1L).username("johnDoe").email("john@example.com").build();
        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername("john@example.com")
                .password("password")
                .authorities("ROLE_USER")
                .build();

        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(userRepository.existsByUsername(request.username())).thenReturn(false);
        when(userMapper.toEntity(request,passwordEncoder)).thenReturn(mappedUser);
        when(userRepository.save(mappedUser)).thenReturn(savedUser);
        when(customUserDetailsService.loadUserByUsername(request.email())).thenReturn(userDetails);
        when(jwtService.generateToken(userDetails)).thenReturn("access-token");
        when(jwtService.generateRefreshToken(userDetails)).thenReturn("refresh-token");
        when(userMapper.toDto(savedUser)).thenReturn(userDto);
        when(mailServiceImpl.sendWelcomeEmailAsync(request.email())).thenReturn(CompletableFuture.completedFuture(null));

        AuthenticationResponse response = authenticationService.registerAsync(request).get();

        assertThat(response.accessToken()).isEqualTo("access-token");
        assertThat(response.refreshToken()).isEqualTo("refresh-token");
        assertThat(response.user()).isEqualTo(userDto);

        verify(tokenRepository).save(any(Token.class));
        verify(mailServiceImpl).sendWelcomeEmailAsync(request.email());
    }

    @Test
    void register_shouldThrowIfEmailExists() {
        RegistrationRequest request = new RegistrationRequest("johnDoe", "john@example.com", "password123");

        when(userRepository.existsByEmail(request.email())).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> authenticationService.registerAsync(request).get());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void authenticate_shouldReturnTokens() throws ExecutionException, InterruptedException {
        AuthenticationRequest request = new AuthenticationRequest("john@example.com", "password123");
        User user = User.builder().id(1L).username("johnDoe").email("john@example.com").build();
        UserDto userDto = UserDto.builder().id(1L).username("johnDoe").email("john@example.com").build();
        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername("john@example.com")
                .password("password")
                .authorities("ROLE_USER")
                .build();

        when(userRepository.findByEmail(request.email())).thenReturn(Optional.of(user));
        when(customUserDetailsService.loadUserByUsername(request.email())).thenReturn(userDetails);
        when(jwtService.generateToken(userDetails)).thenReturn("access-token");
        when(jwtService.generateRefreshToken(userDetails)).thenReturn("refresh-token");
        when(tokenRepository.findAllValidTokensByUser(user.getId().intValue())).thenReturn(Collections.emptyList());
        when(userMapper.toDto(user)).thenReturn(userDto);

        AuthenticationResponse response = authenticationService.authenticateAsync(request).get();

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(tokenRepository, times(2)).save(any(Token.class));
        assertThat(response.accessToken()).isEqualTo("access-token");
        assertThat(response.refreshToken()).isEqualTo("refresh-token");
        assertThat(response.user()).isEqualTo(userDto);
    }
}

