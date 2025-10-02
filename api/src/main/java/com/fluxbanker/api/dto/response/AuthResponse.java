package com.fluxbanker.api.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

/**
 * Unified auth response — matches existing Node contract:
 * { success, message, token, name }
 */
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuthResponse {
    private final boolean success;
    private final String message;
    private final String token;
    private final String name;
}
