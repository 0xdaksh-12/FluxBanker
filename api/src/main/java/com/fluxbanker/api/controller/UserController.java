package com.fluxbanker.api.controller;

import com.fluxbanker.api.dto.response.UserResponse;
import com.fluxbanker.api.entity.User;
import com.fluxbanker.api.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fluxbanker.api.security.SecurityUtils;
import org.springframework.web.bind.annotation.*;
import com.fluxbanker.api.dto.request.UserUpdateRequest;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(Authentication authentication) {
        UUID userId = SecurityUtils.getUserId(authentication);
        if (userId == null)
            return ResponseEntity.status(401).build();
        User user = userService.getUserById(userId);
        return ResponseEntity.ok(UserResponse.fromEntity(user));
    }

    @PatchMapping("/me")
    public ResponseEntity<UserResponse> updateProfile(@RequestBody UserUpdateRequest request, Authentication authentication) {
        UUID userId = SecurityUtils.getUserId(authentication);
        User user = userService.updateUser(userId, request);
        return ResponseEntity.ok(UserResponse.fromEntity(user));
    }

    @PostMapping("/me/verify-email")
    public ResponseEntity<UserResponse> verifyEmail(Authentication authentication) {
        UUID userId = SecurityUtils.getUserId(authentication);
        User user = userService.verifyEmail(userId);
        return ResponseEntity.ok(UserResponse.fromEntity(user));
    }

    @PostMapping("/me/password-reset")
    public ResponseEntity<Void> requestPasswordReset(Authentication authentication) {
        UUID userId = SecurityUtils.getUserId(authentication);
        userService.requestPasswordReset(userId);
        return ResponseEntity.ok().build();
    }
}
