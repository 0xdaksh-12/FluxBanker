package com.fluxbanker.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class KycUpdateRequest {
    @NotBlank(message = "Status is required")
    private String status;
}
