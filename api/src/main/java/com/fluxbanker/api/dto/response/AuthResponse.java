package com.fluxbanker.api.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

/**
 * Unified auth response.
 * Shape: { success, message, token, name, user? }
 */
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuthResponse {
    private final boolean success;
    private final String message;

    /** JWT access token. */
    private final String token;

    private final String name;

    /** Optional user object, populated on registration. */
    private final UserResponse user;
}
