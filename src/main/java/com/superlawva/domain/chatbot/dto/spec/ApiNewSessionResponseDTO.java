package com.superlawva.domain.chatbot.dto.spec;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "새로운 세션 생성 응답 DTO")
public class ApiNewSessionResponseDTO {
        @Schema(description = "생성된 세션의 고유 ID", example = "dc5bc993-861d-4b78-89e6-df356c8f03fb")
        @JsonProperty("session_id")
        String sessionId;

        @Schema(description = "세션 생성 타임스탬프", example = "2025-01-20T15:30:00Z")
        @JsonProperty("created_at")
        String createdAt;

        @Schema(description = "응답 메시지", example = "새로운 채팅 세션이 생성되었습니다.")
        String message;
} 