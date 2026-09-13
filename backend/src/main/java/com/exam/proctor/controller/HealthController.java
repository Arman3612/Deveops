package com.exam.proctor.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;

/**
 * Health Check Controller
 * 
 * Provides a lightweight endpoint for:
 * - Jenkins pipeline deployment verification
 * - Docker container HEALTHCHECK instructions
 * - Nginx upstream health monitoring
 * - Load balancer readiness probes
 */
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class HealthController {

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        return ResponseEntity.ok(Map.of(
            "status", "UP",
            "application", "proctor",
            "timestamp", Instant.now().toString()
        ));
    }
}
