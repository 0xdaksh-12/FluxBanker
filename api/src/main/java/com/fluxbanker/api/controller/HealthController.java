package com.fluxbanker.api.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Simple controller to handle health checks and root requests.
 * Prevents NoResourceFoundException in logs when root is accessed.
 */
@RestController
public class HealthController {

    @GetMapping("/")
    public Map<String, String> home() {
        return Map.of(
            "status", "UP",
            "message", "FluxBanker API is running",
            "version", "1.0.0"
        );
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "UP");
    }
}
