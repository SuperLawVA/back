package com.superlawva.domain.chatbot.dto.spec;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "채팅 이력의 개별 메시지 항목 DTO")
public class ApiChatMessageHistoryDTO {
    @Schema(description = "사용자가 보낸 메시지", example = "임대차 보증금 문제가 있어요.")
    @JsonProperty("user_message")
    String userMessage;

    @Schema(description = "챗봇이 응답한 메시지", example = "어떤 점이 궁금하신가요?")
    @JsonProperty("bot_response")
    String botResponse;

    @Schema(description = "타임스탬프", example = "2025-01-20T15:30:45Z")
    String timestamp;
    
    @Schema(description = "질문의 유형", example = "legal_rag")
    @JsonProperty("question_type")
    String questionType;
} 