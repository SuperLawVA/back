package com.superlawva.global.controller;

import com.superlawva.domain.ml.client.MLApiClient;
import com.superlawva.global.response.ApiResponse;
import com.superlawva.global.response.status.SuccessStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@Slf4j
@Tag(name = "🏥 Health Check", description = "서비스 상태 확인 API")
public class HealthController {

    private final DataSource dataSource;
    private final MLApiClient mlApiClient;
    
    // Redis는 선택적 의존성으로 변경
    @Autowired(required = false)
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 기본 헬스체크 - Actuator 대체용 (구동 우선)
     * GET /health
     */
    @Operation(summary = "기본 헬스체크", description = "애플리케이션의 기본 상태를 확인합니다.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "헬스체크 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponse.class),
                examples = @ExampleObject(
                    name = "성공 응답",
                    value = """
                    {
                        "success": true,
                        "message": "애플리케이션이 정상적으로 실행 중입니다.",
                        "data": {
                            "status": "UP",
                            "timestamp": "2024-01-15T10:30:00",
                            "service": "SuperLawVA Backend",
                            "version": "1.0.0",
                            "message": "애플리케이션이 정상적으로 실행 중입니다."
                        }
                    }
                    """
                )
            )
        )
    })
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<Map<String, Object>>> basicHealthCheck() {
        try {
            Map<String, Object> health = new HashMap<>();
            health.put("status", "UP");
            health.put("timestamp", LocalDateTime.now());
            health.put("service", "SuperLawVA Backend");
            health.put("version", "1.0.0");
            health.put("message", "애플리케이션이 정상적으로 실행 중입니다.");
            
            log.info("헬스체크 요청 성공");
            return ResponseEntity.ok(ApiResponse.of(SuccessStatus._OK, health));
        } catch (Exception e) {
            log.error("헬스체크 중 오류 발생", e);
            Map<String, Object> health = new HashMap<>();
            health.put("status", "UP"); // 구동 우선이므로 UP 반환
            health.put("timestamp", LocalDateTime.now());
            health.put("service", "SuperLawVA Backend");
            health.put("message", "기본 헬스체크 성공");
            return ResponseEntity.ok(ApiResponse.of(SuccessStatus._OK, health));
        }
    }

    /**
     * 상세 헬스체크 - 모든 의존성 확인 (구동 우선)
     * GET /health/detailed
     */
    @Operation(summary = "상세 헬스체크", description = "모든 의존성(데이터베이스, Redis, ML API)의 상태를 확인합니다.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "상세 헬스체크 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponse.class),
                examples = @ExampleObject(
                    name = "성공 응답",
                    value = """
                    {
                        "success": true,
                        "message": "상세 헬스체크가 완료되었습니다.",
                        "data": {
                            "status": "UP",
                            "timestamp": "2024-01-15T10:30:00",
                            "service": "SuperLawVA Backend",
                            "version": "1.0.0",
                            "components": {
                                "database": {
                                    "status": "UP",
                                    "details": "MySQL connection successful"
                                },
                                "redis": {
                                    "status": "UP",
                                    "details": "Redis connection successful"
                                },
                                "ml-api": {
                                    "status": "UP",
                                    "details": "ML API connection successful"
                                }
                            }
                        }
                    }
                    """
                )
            )
        )
    })
    @GetMapping("/health/detailed")
    public ResponseEntity<ApiResponse<Map<String, Object>>> detailedHealthCheck() {
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
        if (redisTemplate != null) {
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
        } else {
            components.put("redis", Map.of("status", "N/A", "details", "Redis not configured"));
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
        
        return ResponseEntity.ok(ApiResponse.of(SuccessStatus._OK, health));
    }
    
    /**
     * 루트 경로 헬스체크 - 브라우저 접근용
     * GET /
     */
    @Operation(summary = "루트 경로 헬스체크", description = "브라우저 접근용 기본 상태 확인 및 API 엔드포인트 정보를 제공합니다.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "루트 경로 접근 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponse.class),
                examples = @ExampleObject(
                    name = "성공 응답",
                    value = """
                    {
                        "success": true,
                        "message": "백엔드 서비스가 정상적으로 실행 중입니다.",
                        "data": {
                            "service": "SuperLawVA Backend API",
                            "status": "running",
                            "timestamp": "2024-01-15T10:30:00",
                            "message": "백엔드 서비스가 정상적으로 실행 중입니다.",
                            "endpoints": {
                                "health": "/health",
                                "detailed-health": "/health/detailed",
                                "status": "/status",
                                "docs": "/swagger-ui/index.html"
                            }
                        }
                    }
                    """
                )
            )
        )
    })
    @GetMapping("/")
    public ResponseEntity<ApiResponse<Map<String, Object>>> rootHealthCheck() {
        Map<String, Object> response = new HashMap<>();
        response.put("service", "SuperLawVA Backend API");
        response.put("status", "running");
        response.put("timestamp", LocalDateTime.now());
        response.put("message", "백엔드 서비스가 정상적으로 실행 중입니다.");
        response.put("endpoints", Map.of(
            "health", "/health",
            "detailed-health", "/health/detailed",
            "status", "/status",
            "docs", "/swagger-ui/index.html"
        ));
        
        log.info("루트 경로 접근");
        return ResponseEntity.ok(ApiResponse.of(SuccessStatus._OK, response));
    }

    @GetMapping("/health/time")
    public Map<String, String> getServerTime() {
        Map<String, String> result = new HashMap<>();
        ZonedDateTime now = ZonedDateTime.now();
        result.put("serverTime", now.format(DateTimeFormatter.ISO_ZONED_DATE_TIME));
        result.put("timeZone", now.getZone().toString());
        result.put("systemMillis", String.valueOf(System.currentTimeMillis()));
        return result;
    }
} 