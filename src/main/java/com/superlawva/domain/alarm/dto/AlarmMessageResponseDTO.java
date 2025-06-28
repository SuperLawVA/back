package com.superlawva.domain.alarm.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "알림 메시지 응답 DTO")
public class AlarmMessageResponseDTO {
    
    @Schema(description = "응답 메시지", example = "모든 알림을 읽음 처리했습니다.")
    private String message;
    
    @Schema(description = "처리된 알림 개수", example = "5")
    private Integer processedCount;
    
    @Schema(description = "처리 시간", example = "2024-01-15T10:30:00")
    private String processedAt;
} 