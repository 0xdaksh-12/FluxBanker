package com.fluxbanker.api.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import java.util.UUID;

public class SecurityUtils {

    public static UUID getUserId(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            return null;
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof CustomUserDetails) {
            return ((CustomUserDetails) principal).getUserId();
        }

        // Handle default Spring Security User (often used in tests)
        if (principal instanceof User) {
            try {
                return UUID.fromString(((User) principal).getUsername());
            } catch (IllegalArgumentException e) {
                // If username is not a UUID, we can't extract userId from it
                return null;
            }
        }

        return null;
    }
}
