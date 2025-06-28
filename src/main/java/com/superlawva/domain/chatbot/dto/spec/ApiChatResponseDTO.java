package com.superlawva.domain.chatbot.dto.spec;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "챗봇 대화 응답 DTO")
public class ApiChatResponseDTO {
        @Schema(description = "챗봇의 답변 내용", example = "임대차 보증금 반환을 위해서는...")
        String answer;

        @Schema(description = "질문의 유형", example = "legal_rag")
        @JsonProperty("question_type")
        String questionType;

        @Schema(description = "응답 소요 시간(초)", example = "2.5")
        @JsonProperty("response_time_seconds")
        Double responseTimeSeconds;

        @Schema(description = "현재 대화의 세션 ID", example = "dc5bc993-861d-4b78-89e6-df356c8f03fb")
        @JsonProperty("session_id")
        String sessionId;

        @Schema(description = "응답 타임스탬프", example = "2025-01-20T15:30:45Z")
        String timestamp;
} 