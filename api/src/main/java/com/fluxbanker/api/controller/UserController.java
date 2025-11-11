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

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@lombok.extern.slf4j.Slf4j
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(@AuthenticationPrincipal CustomUserDetails userDetails) {
        User user = userService.getUserById(userDetails.getUserId());
        return ResponseEntity.ok(UserResponse.fromEntity(user));
    }

    @PatchMapping("/me")
    public ResponseEntity<UserResponse> updateProfile(@RequestBody UserUpdateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null)
            return ResponseEntity.status(401).build();
        User user = userService.updateUser(userDetails.getUserId(), request);
        return ResponseEntity.ok(UserResponse.fromEntity(user));
    }

    @PostMapping("/me/verify-email")
    public ResponseEntity<UserResponse> verifyEmail(@AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null)
            return ResponseEntity.status(401).build();
        log.info("Requesting email verification for user: {}", userDetails.getUserId());
        User user = userService.sendVerificationEmail(userDetails.getUserId());
        return ResponseEntity.ok(UserResponse.fromEntity(user));
    }

    @PostMapping("/{userId}/kyc")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> updateKycStatus(
            @PathVariable java.util.UUID userId,
            @RequestBody java.util.Map<String, String> request) {

        String statusStr = request.get("status");
        if (statusStr == null) {
            return ResponseEntity.badRequest().build();
        }

        User.KycStatus status = User.KycStatus.valueOf(statusStr.toUpperCase());
        User user = userService.updateKycStatus(userId, status);

        return ResponseEntity.ok(UserResponse.fromEntity(user));
    }
}
