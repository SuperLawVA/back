package com.superlawva.domain.chatbot.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "세션 목록 응답")
public record SessionListResponseDTO(
        
        @Schema(description = "세션 ID", example = "dc5bc993-861d-4b78-89e6-df356c8f03fb")
        String sessionId,
        
        @Schema(description = "세션 제목 (첫 번째 질문 기반)", example = "임대차 보증금 관련 상담")
        String title,
        
        @Schema(description = "마지막 활동 시간", example = "2025-01-20T15:30:45.123456")
        LocalDateTime last_datetime
) {
} 