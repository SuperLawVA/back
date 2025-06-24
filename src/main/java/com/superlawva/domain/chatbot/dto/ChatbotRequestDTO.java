package com.superlawva.domain.chatbot.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "챗봇 메시지 요청 (ML 팀 API 스펙)")
public record ChatbotRequestDTO(
        
        @Schema(description = "사용자 메시지", example = "임대차 보증금을 안돌려줘요")
        @NotBlank(message = "메시지는 필수입니다.")
        @Size(max = 1000, message = "메시지는 1000자 이하여야 합니다.")
        String message,
        
        @Schema(description = "세션 ID (옵션, 없으면 자동 생성)", example = "dc5bc993-861d-4b78-89e6-df356c8f03fb")
        String session_id
) {} 