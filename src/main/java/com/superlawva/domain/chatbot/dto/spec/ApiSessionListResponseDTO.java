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
@Schema(description = "활성 세션 목록 조회 응답 DTO")
public class ApiSessionListResponseDTO {
        @Schema(description = "전체 활성 세션 수", example = "2")
        @JsonProperty("total_sessions")
        int totalSessions;

        @Schema(description = "세션 목록")
        List<ApiSessionListItemDTO> sessions;
        
        // 명시적으로 필요한 getter 메서드들 추가
        public int getTotalSessions() {
            return totalSessions;
        }
        
        public List<ApiSessionListItemDTO> getSessions() {
            return sessions;
        }
} 