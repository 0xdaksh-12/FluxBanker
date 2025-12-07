package com.fluxbanker.api.controller;

import com.fluxbanker.api.dto.response.UserResponse;
import com.fluxbanker.api.entity.User;
import com.fluxbanker.api.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fluxbanker.api.security.CustomUserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import com.fluxbanker.api.dto.request.UserUpdateRequest;
import com.fluxbanker.api.dto.request.KycUpdateRequest;
import com.fluxbanker.api.exception.UnauthorizedException;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@lombok.extern.slf4j.Slf4j
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(@AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null)
            throw new UnauthorizedException("User not authenticated");
        User user = userService.getUserById(userDetails.getUserId());
        return ResponseEntity.ok(UserResponse.fromEntity(user));
    }

    @PatchMapping("/me")
    public ResponseEntity<UserResponse> updateProfile(@RequestBody UserUpdateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null)
            throw new UnauthorizedException("User not authenticated");
        User user = userService.updateUser(userDetails.getUserId(), request);
        return ResponseEntity.ok(UserResponse.fromEntity(user));
    }

    @PostMapping("/me/verify-email")
    public ResponseEntity<UserResponse> verifyEmail(@AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null)
            throw new UnauthorizedException("User not authenticated");
        User user = userService.sendVerificationEmail(userDetails.getUserId());
        return ResponseEntity.ok(UserResponse.fromEntity(user));
    }

    @PostMapping("/{userId}/kyc")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> updateKycStatus(
            @PathVariable java.util.UUID userId,
            @Valid @RequestBody KycUpdateRequest request) {

        String statusStr = request.getStatus();

        try {
            User.KycStatus status = User.KycStatus.valueOf(statusStr.toUpperCase());
            User user = userService.updateKycStatus(userId, status);
            return ResponseEntity.ok(UserResponse.fromEntity(user));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid KYC status: " + statusStr);
        }
    }
}
