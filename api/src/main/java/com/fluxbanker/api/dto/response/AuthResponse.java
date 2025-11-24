package com.fluxbanker.api.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

/**
 * Unified auth response.
 * Shape: { success, message, token, name, user? }
 */
@Getter
@Builder
@ToString(exclude = {"token", "user"})
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuthResponse {
    private final boolean success;
    private final String message;

    /** JWT access token. */
    private final String token;


    /** Optional user object, populated on registration. */
    private final UserResponse user;
}
