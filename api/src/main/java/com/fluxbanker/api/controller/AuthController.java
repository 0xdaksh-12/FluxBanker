package com.fluxbanker.api.controller;

import com.fluxbanker.api.dto.request.ForgotPasswordRequest;
import com.fluxbanker.api.dto.request.LoginRequest;
import com.fluxbanker.api.dto.request.RegisterRequest;
import com.fluxbanker.api.dto.request.ResetPasswordRequest;
import com.fluxbanker.api.dto.response.AuthResponse;
import com.fluxbanker.api.dto.response.UserResponse;
import com.fluxbanker.api.entity.User;
import com.fluxbanker.api.service.AuthService;
import com.fluxbanker.api.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Auth REST controller — exposes the same endpoints as the Node authRoutes.js.
 * Response shape matches: { success, message, accessToken, name, user? }
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserService userService;

    /** POST /auth/register */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request,
            HttpServletRequest req,
            HttpServletResponse res) {
        String accessToken = authService.register(request, req, res);
        User createdUser = userService.getUserByEmail(request.getEmail());
        return ResponseEntity.status(HttpStatus.CREATED).body(
                AuthResponse.builder()
                        .success(true)
                        .message("User registered successfully")
                        .token(accessToken)
                        .user(UserResponse.fromEntity(createdUser))
                        .build());
    }

    /** POST /auth/login */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request,
            HttpServletRequest req,
            HttpServletResponse res) {
        String accessToken = authService.login(request, req, res);
        return ResponseEntity.ok(
                AuthResponse.builder()
                        .success(true)
                        .message("User logged in successfully")
                        .token(accessToken)
                        .build());
    }

    /** POST /auth/logout — requires valid access token */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(Authentication authentication, HttpServletResponse res) {
        UUID sessionId = UUID.fromString((String) authentication.getCredentials());
        authService.logout(sessionId, res);
        return ResponseEntity.noContent().build();
    }

    /** POST /auth/refresh — reads refresh token from cookie */
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(HttpServletRequest req, HttpServletResponse res) {
        String accessToken = authService.refresh(req, res);
        if (accessToken == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(
                AuthResponse.builder()
                        .success(true)
                        .message("Token refreshed successfully")
                        .token(accessToken)
                        .build());
    }

    /** POST /auth/forgot-password */
    @PostMapping("/forgot-password")
    public ResponseEntity<AuthResponse> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request);
        return ResponseEntity.ok(
                AuthResponse.builder()
                        .success(true)
                        .message("Password reset link sent to your email")
                        .build());
    }

    /** GET /auth/validate-reset-token — used to check if token is still valid before showing reset form */
    @GetMapping("/validate-reset-token")
    public ResponseEntity<AuthResponse> validateResetToken(@RequestParam String token) {
        authService.validateResetToken(token);
        return ResponseEntity.ok(
                AuthResponse.builder()
                        .success(true)
                        .message("Token is valid")
                        .build());
    }

    /** GET /auth/verify-email — used to verify the user's email address */
    @GetMapping("/verify-email")
    public ResponseEntity<AuthResponse> verifyEmail(@RequestParam String token) {
        userService.verifyEmailToken(token);
        return ResponseEntity.ok(
                AuthResponse.builder()
                        .success(true)
                        .message("Email verified successfully")
                        .build());
    }

    /** POST /auth/reset-password */
    @PostMapping("/reset-password")
    public ResponseEntity<AuthResponse> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ResponseEntity.ok(
                AuthResponse.builder()
                        .success(true)
                        .message("Password reset successfully")
                        .build());
    }


}
