package com.superlawva.domain.status.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

@Schema(description = "ML 서비스 상태 확인 응답 (ML 팀 API 스펙)")
public record StatusResponseDTO(
        
        @Schema(description = "서비스 상태", example = "running")
        String status,
        
        @Schema(description = "활성 세션 수", example = "42")
        @JsonProperty("active_sessions")
        Integer activeSessions,
        
        @Schema(description = "모델 상태 정보")
        ModelStatus models,
        
        @Schema(description = "벡터 검색 준비 상태", example = "true")
        @JsonProperty("vector_search_ready")
        Boolean vectorSearchReady,
        
        @Schema(description = "상태 확인 시간", example = "2025-06-20T15:49:27.843808")
        LocalDateTime timestamp
) {
    
    @Schema(description = "ML 모델 상태")
    public record ModelStatus(
            
            @Schema(description = "경량 모델 상태", example = "healthy")
            String light,
            
            @Schema(description = "무거운 모델 상태", example = "healthy")
            String heavy,
            
            @Schema(description = "마지막 헬스체크", example = "2025-06-20T15:49:00")
            @JsonProperty("last_health_check")
            LocalDateTime lastHealthCheck
    ) {}
    
    public static StatusResponseDTO running(Integer activeSessions, Boolean vectorReady) {
        return new StatusResponseDTO(
                "running",
                activeSessions,
                new ModelStatus("healthy", "healthy", LocalDateTime.now()),
                vectorReady,
                LocalDateTime.now()
        );
    }
    
    public static StatusResponseDTO degraded(Integer activeSessions, String issue) {
        return new StatusResponseDTO(
                "degraded",
                activeSessions,
                new ModelStatus("warning", "warning", LocalDateTime.now()),
                false,
                LocalDateTime.now()
        );
    }
    
    public static StatusResponseDTO down() {
        return new StatusResponseDTO(
                "down",
                0,
                new ModelStatus("error", "error", LocalDateTime.now()),
                false,
                LocalDateTime.now()
        );
    }
} 