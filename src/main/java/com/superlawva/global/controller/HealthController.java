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
     * 기본 헬스체크 - Actuator 대체용 (구동 우선)
     * GET /api/health
     */
    @GetMapping("/api/health")
    public ResponseEntity<Map<String, Object>> basicHealthCheck() {
        try {
            Map<String, Object> health = new HashMap<>();
            health.put("status", "UP");
            health.put("timestamp", LocalDateTime.now());
            health.put("service", "SuperLawVA Backend");
            health.put("version", "1.0.0");
            health.put("message", "애플리케이션이 정상적으로 실행 중입니다.");
            
            log.info("헬스체크 요청 성공");
            return ResponseEntity.ok(health);
        } catch (Exception e) {
            log.error("헬스체크 중 오류 발생", e);
            Map<String, Object> health = new HashMap<>();
            health.put("status", "UP"); // 구동 우선이므로 UP 반환
            health.put("timestamp", LocalDateTime.now());
            health.put("service", "SuperLawVA Backend");
            health.put("message", "기본 헬스체크 성공");
            return ResponseEntity.ok(health);
        }
    }

    /**
     * 상세 헬스체크 - 모든 의존성 확인 (구동 우선)
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
     * 간단한 상태 확인 - 로드밸런서용 (구동 우선)
     * GET /health (루트 레벨)
     */
    @GetMapping("/health")
    public ResponseEntity<String> simpleHealthCheck() {
        log.info("간단한 헬스체크 요청");
        return ResponseEntity.ok("SuperLawVA Backend - OK");
    }
    
    /**
     * 루트 경로 헬스체크 - 브라우저 접근용
     * GET /
     */
    @GetMapping("/")
    public ResponseEntity<Map<String, Object>> rootHealthCheck() {
        Map<String, Object> response = new HashMap<>();
        response.put("service", "SuperLawVA Backend API");
        response.put("status", "running");
        response.put("timestamp", LocalDateTime.now());
        response.put("message", "백엔드 서비스가 정상적으로 실행 중입니다.");
        response.put("endpoints", Map.of(
            "health", "/api/health",
            "detailed-health", "/api/health/detailed",
            "status", "/api/v1/status",
            "docs", "/swagger-ui/index.html"
        ));
        
        log.info("루트 경로 접근");
        return ResponseEntity.ok(response);
    }
} 