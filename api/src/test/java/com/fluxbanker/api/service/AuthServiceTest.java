package com.fluxbanker.api.service;

import com.fluxbanker.api.dto.request.RegisterRequest;
import com.fluxbanker.api.entity.Session;
import com.fluxbanker.api.entity.User;
import com.fluxbanker.api.exception.ConflictException;
import com.fluxbanker.api.exception.UnauthorizedException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserService userService;
    @Mock
    private SessionService sessionService;
    @Mock
    private JwtService jwtService;
    @Mock
    private BCryptPasswordEncoder passwordEncoder;
    @Mock
    private AccountService accountService;
    @Mock
    private HttpServletRequest httpRequest;
    @Mock
    private HttpServletResponse httpResponse;

    @InjectMocks
    private AuthService authService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authService, "refreshExpirationMs", 604_800_000L);
        ReflectionTestUtils.setField(authService, "nodeEnv", "development");
    }

    @Test
    void register_shouldCreateUserAndReturnToken() {
        RegisterRequest req = new RegisterRequest();
        req.setFirstName("Test");
        req.setLastName("User");
        req.setEmail("test@example.com");
        req.setPassword("password123");

        User savedUser = User.builder().id(UUID.randomUUID()).firstName("Test").lastName("User").email("test@example.com")
                .password("hashed").build();
        Session session = Session.builder().id(UUID.randomUUID()).user(savedUser).build();

        when(userService.existsByEmail("test@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("hashed");
        when(userService.createUser(any(User.class))).thenReturn(savedUser);
        when(httpRequest.getHeader("User-Agent")).thenReturn("TestAgent");
        when(httpRequest.getRemoteAddr()).thenReturn("127.0.0.1");
        when(sessionService.createSession(any(), any(), any())).thenReturn(session);
        when(jwtService.generateAccessToken(any(), any())).thenReturn("access-token");
        when(jwtService.generateRefreshToken(any(), any())).thenReturn("refresh-token");

        String token = authService.register(req, httpRequest, httpResponse);

        assertThat(token).isEqualTo("access-token");
        verify(sessionService).saveToken(session.getId(), "refresh-token");
    }

    @Test
    void register_shouldThrowConflict_whenEmailTaken() {
        RegisterRequest req = new RegisterRequest();
        req.setEmail("taken@example.com");

        when(userService.existsByEmail("taken@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(req, httpRequest, httpResponse))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("User already exists");
    }

    @Test
    void login_shouldReturnToken_whenCredentialsValid() {
        com.fluxbanker.api.dto.request.LoginRequest req = new com.fluxbanker.api.dto.request.LoginRequest();
        req.setEmail("user@example.com");
        req.setPassword("secret");

        User user = User.builder().id(UUID.randomUUID()).firstName("Alice").lastName("Smith").email("user@example.com").password("hashed")
                .build();
        Session session = Session.builder().id(UUID.randomUUID()).user(user).build();

        when(userService.getUserByEmail("user@example.com")).thenReturn(user);
        when(passwordEncoder.matches("secret", "hashed")).thenReturn(true);
        when(httpRequest.getHeader("User-Agent")).thenReturn("TestAgent");
        when(httpRequest.getRemoteAddr()).thenReturn("127.0.0.1");
        when(sessionService.createSession(any(), any(), any())).thenReturn(session);
        when(jwtService.generateAccessToken(any(), any())).thenReturn("access-token");
        when(jwtService.generateRefreshToken(any(), any())).thenReturn("refresh-token");

        String accessToken = authService.login(req, httpRequest, httpResponse);

        assertThat(accessToken).isEqualTo("access-token");
    }

    @Test
    void login_shouldThrowUnauthorized_whenPasswordWrong() {
        com.fluxbanker.api.dto.request.LoginRequest req = new com.fluxbanker.api.dto.request.LoginRequest();
        req.setEmail("user@example.com");
        req.setPassword("wrong");

        User user = User.builder().id(UUID.randomUUID()).email("user@example.com").password("hashed").build();

        when(userService.getUserByEmail("user@example.com")).thenReturn(user);
        when(passwordEncoder.matches("wrong", "hashed")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(req, httpRequest, httpResponse))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("Invalid credentials");
    }
}
