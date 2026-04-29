package com.fluxbanker.api.controller;

import com.fluxbanker.api.dto.request.LoginRequest;
import com.fluxbanker.api.dto.request.RegisterRequest;
import com.fluxbanker.api.dto.response.AuthResponse;
import com.fluxbanker.api.service.AuthService;
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
 * Response shape matches: { success, message, token, name }
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /** POST /auth/register */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request,
            HttpServletRequest req,
            HttpServletResponse res) {
        // getUserByEmail is called inside register to get the name — we re-fetch after
        // creation
        // AuthService returns the accessToken; user name is in the request body
        String accessToken = authService.register(request, req, res);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                AuthResponse.builder()
                        .success(true)
                        .message("User registered successfully")
                        .token(accessToken)
                        .name(request.getFirstName() + " " + request.getLastName())
                        .build());
    }

    /** POST /auth/login */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request,
            HttpServletRequest req,
            HttpServletResponse res) {
        String[] result = authService.loginWithName(request, req, res);
        return ResponseEntity.ok(
                AuthResponse.builder()
                        .success(true)
                        .message("User logged in successfully")
                        .token(result[0])
                        .name(result[1])
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
        return ResponseEntity.status(HttpStatus.CREATED).body(
                AuthResponse.builder()
                        .success(true)
                        .message("Token refreshed successfully")
                        .token(accessToken)
                        .build());
    }

}
