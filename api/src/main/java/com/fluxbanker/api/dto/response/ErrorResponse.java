package com.fluxbanker.api.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

/** Standardised error envelope — mirrors Node's errorHandler response format. */
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {
    private final boolean success;
    private final String message;
    private final List<FieldError> errors;

    /** Individual field-level validation error. */
    @Getter
    @Builder
    public static class FieldError {
        private final String path;
        private final String message;
    }
}
