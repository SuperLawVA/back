package com.superlawva.domain.chatbot.dto.spec;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "세션별 채팅 이력 조회 응답 DTO")
public class ApiSessionHistoryResponseDTO {
        @Schema(description = "조회한 세션의 고유 ID", example = "dc5bc993-861d-4b78-89e6-df356c8f03fb")
        @JsonProperty("session_id")
        String sessionId;

        @Schema(description = "세션의 전체 메시지 수", example = "10")
        @JsonProperty("total_messages")
        int totalMessages;

        @Schema(description = "채팅 이력 목록")
        @JsonProperty("chat_history")
        List<ApiChatMessageHistoryDTO> chatHistory;

        public String getSessionId() {
            return sessionId;
        }
        
        public int getTotalMessages() {
            return totalMessages;
        }
        
        public List<ApiChatMessageHistoryDTO> getChatHistory() {
            return chatHistory;
        }
} 