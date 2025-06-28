package com.superlawva.domain.chatbot.dto.spec;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "세션 삭제 응답 DTO")
public class ApiDeleteSessionResponseDTO {
        @Schema(description = "결과 메시지", example = "세션 uuid-string이 삭제되었습니다.")
        String message;
        
        // 명시적으로 필요한 getter 메서드들 추가
        public String getMessage() {
            return message;
        }
} 