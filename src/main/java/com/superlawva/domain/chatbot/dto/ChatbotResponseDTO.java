package com.superlawva.domain.chatbot.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

@Schema(description = "챗봇 응답 (ML 팀 API 스펙)")
public record ChatbotResponseDTO(
        
        @Schema(description = "챗봇 답변", example = "## 🏠 상황 정리\n임대차 보증금을 돌려받지 못한 상황으로...")
        String answer,
        
        @Schema(description = "세션 ID", example = "dc5bc993-861d-4b78-89e6-df356c8f03fb")
        @JsonProperty("session_id")
        String sessionId,
        
        @Schema(description = "응답 생성 시간", example = "2025-06-20T15:49:27.843808")
        LocalDateTime timestamp,
        
        @Schema(description = "API 응답 시간(초)", example = "3.47")
        @JsonProperty("response_time_seconds")
        Double responseTimeSeconds,
        
        @Schema(description = "질문 유형", example = "first_chat")
        @JsonProperty("question_type")
        String questionType,
        
        @Schema(description = "토큰 사용량 정보", example = "{\"input_tokens\": 25, \"output_tokens\": 150}")
        @JsonProperty("token_usage")
        String tokenUsage,
        
        @Schema(description = "응답 성공 여부", example = "true")
        Boolean success
) {
    
    public static ChatbotResponseDTO from(String answer, String sessionId, String questionType, Double responseTimeSeconds) {
        return new ChatbotResponseDTO(
                answer,
                sessionId,
                LocalDateTime.now(),
                responseTimeSeconds,
                questionType,
                null, // tokenUsage - ML API에서 제공시 설정
                true  // success
        );
    }
    
    public static ChatbotResponseDTO error(String errorMessage, String sessionId) {
        return new ChatbotResponseDTO(
                errorMessage,
                sessionId,
                LocalDateTime.now(),
                null,
                "error",
                null,
                false
        );
    }
} 