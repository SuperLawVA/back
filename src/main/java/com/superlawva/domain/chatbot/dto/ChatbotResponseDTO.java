package com.superlawva.domain.chatbot.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.superlawva.domain.chatbot.entity.ChatMessageEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "챗봇 API 응답 DTO")
public class ChatbotResponseDTO {
    @Schema(description = "챗봇의 최종 답변", example = "임대차 계약서의 해당 조항은...")
    String answer;

    @Schema(description = "현재 대화의 세션 ID", example = "dc5bc993-861d-4b78-89e6-df356c8f03fb")
    @JsonProperty("session_id")
    String sessionId;

    @Schema(description = "질문의 유형(ML 팀 정의)", example = "legal_rag")
    @JsonProperty("question_type")
    String questionType;

    @Schema(description = "응답 소요 시간(초)", example = "2.5")
    @JsonProperty("response_time_seconds")
    Double responseTimeSeconds;

    @Schema(hidden = true)
    ChatMessageEntity messageEntity;

    // 명시적으로 필요한 getter 메서드들 추가
    public String getAnswer() {
        return answer;
    }
    
    public String getSessionId() {
        return sessionId;
    }
    
    public String getQuestionType() {
        return questionType;
    }
    
    public Double getResponseTimeSeconds() {
        return responseTimeSeconds;
    }
    
    public ChatMessageEntity getMessageEntity() {
        return messageEntity;
    }
} 