package com.capstone.adaptivelearning.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class HealthController {

    @Value("${ml-service.url:http://localhost:8000}")
    private String mlServiceUrl;

    public record HealthResponse(
            String status,
            String service,
            String javaVersion,
            String mlServiceConfiguredUrl,
            Instant timestamp
    ) {}

    @GetMapping("/health")
    public ResponseEntity<HealthResponse> checkHealth() {
        return ResponseEntity.ok(new HealthResponse(
                "UP",
                "adaptive-learning-backend",
                System.getProperty("java.version"),
                mlServiceUrl,
                Instant.now()
        ));
    }
}

