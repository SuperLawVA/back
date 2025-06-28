package com.superlawva.domain.chatbot.dto.spec;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "활성 세션 목록의 개별 항목 DTO")
public class ApiSessionListItemDTO {
        @Schema(description = "세션 고유 ID", example = "dc5bc993-861d-4b78-89e6-df356c8f03fb")
        @JsonProperty("session_id")
        String sessionId;

        @Schema(description = "세션 생성 타임스탬프", example = "2025-01-20T15:30:00Z")
        @JsonProperty("created_at")
        String createdAt;

        @Schema(description = "세션 내 메시지 수", example = "5")
        @JsonProperty("message_count")
        int messageCount;

        @Schema(description = "마지막 질문 유형", example = "case_rag")
        @JsonProperty("last_question_type")
        String lastQuestionType;
} 