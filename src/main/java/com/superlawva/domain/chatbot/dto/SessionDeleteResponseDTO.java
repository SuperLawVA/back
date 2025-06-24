package com.superlawva.domain.chatbot.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "세션 삭제 응답 (ML 팀 API 스펙)")
public record SessionDeleteResponseDTO(
        
        @Schema(description = "삭제 완료 메시지", example = "세션이 성공적으로 삭제되었습니다.")
        String message
) {
    
    public static SessionDeleteResponseDTO success(String sessionId) {
        return new SessionDeleteResponseDTO(
                "세션 '" + sessionId + "'이 성공적으로 삭제되었습니다."
        );
    }
    
    public static SessionDeleteResponseDTO notFound() {
        return new SessionDeleteResponseDTO(
                "해당 세션을 찾을 수 없습니다."
        );
    }
    
    public static SessionDeleteResponseDTO unauthorized() {
        return new SessionDeleteResponseDTO(
                "세션을 삭제할 권한이 없습니다."
        );
    }
} 