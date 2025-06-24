package com.superlawva.global.controller;

import com.superlawva.domain.ml.client.MLApiClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@Slf4j
public class HealthController {

    private final DataSource dataSource;
    private final RedisTemplate<String, Object> redisTemplate;
    private final MLApiClient mlApiClient;

    /**
     * 기본 헬스체크 - Actuator 대체용
     * GET /api/health
     */
    @GetMapping("/api/health")
    public ResponseEntity<Map<String, Object>> basicHealthCheck() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("timestamp", LocalDateTime.now());
        health.put("service", "SuperLawVA Backend");
        health.put("version", "1.0.0");
        
        return ResponseEntity.ok(health);
    }

    /**
     * 상세 헬스체크 - 모든 의존성 확인
     * GET /api/health/detailed
     */
    @GetMapping("/api/health/detailed")
    public ResponseEntity<Map<String, Object>> detailedHealthCheck() {
        Map<String, Object> health = new HashMap<>();
        Map<String, Object> components = new HashMap<>();
        
        boolean allHealthy = true;
        
        // Database 상태 확인
        try {
            try (Connection connection = dataSource.getConnection()) {
                if (connection.isValid(5)) {
                    components.put("database", Map.of("status", "UP", "details", "MySQL connection successful"));
                } else {
                    components.put("database", Map.of("status", "DOWN", "details", "MySQL connection invalid"));
                    allHealthy = false;
                }
            }
        } catch (Exception e) {
            components.put("database", Map.of("status", "DOWN", "details", "MySQL connection failed: " + e.getMessage()));
            allHealthy = false;
            log.warn("Database health check failed", e);
        }
        
        // Redis 상태 확인
        try {
            redisTemplate.opsForValue().set("health:check", "ok");
            String result = (String) redisTemplate.opsForValue().get("health:check");
            if ("ok".equals(result)) {
                components.put("redis", Map.of("status", "UP", "details", "Redis connection successful"));
            } else {
                components.put("redis", Map.of("status", "DOWN", "details", "Redis connection test failed"));
                allHealthy = false;
            }
        } catch (Exception e) {
            components.put("redis", Map.of("status", "DOWN", "details", "Redis connection failed: " + e.getMessage()));
            allHealthy = false;
            log.warn("Redis health check failed", e);
        }
        
        // ML API 상태 확인
        try {
            boolean mlHealthy = mlApiClient.isHealthy();
            if (mlHealthy) {
                components.put("ml-api", Map.of("status", "UP", "details", "ML API connection successful"));
            } else {
                components.put("ml-api", Map.of("status", "DOWN", "details", "ML API connection failed"));
                // ML API는 필수가 아니므로 전체 상태에 영향주지 않음
                log.warn("ML API health check failed");
            }
        } catch (Exception e) {
            components.put("ml-api", Map.of("status", "DOWN", "details", "ML API connection error: " + e.getMessage()));
            log.warn("ML API health check error", e);
        }
        
        // 전체 상태 설정
        health.put("status", allHealthy ? "UP" : "DOWN");
        health.put("timestamp", LocalDateTime.now());
        health.put("service", "SuperLawVA Backend");
        health.put("version", "1.0.0");
        health.put("components", components);
        
        return ResponseEntity.ok(health);
    }

    /**
     * 간단한 상태 확인 - 로드밸런서용
     * GET /health (루트 레벨)
     */
    @GetMapping("/health")
    public ResponseEntity<String> simpleHealthCheck() {
        return ResponseEntity.ok("OK");
    }
} 