package com.superlawva.domain.chatbot.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "새 세션 생성 요청")
public record SessionCreateRequestDTO(
        
        @Schema(description = "사용자 ID", example = "123")
        @NotNull(message = "사용자 ID는 필수입니다.")
        Long userId
) {
} 