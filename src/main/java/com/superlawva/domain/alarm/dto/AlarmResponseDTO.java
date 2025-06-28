package com.superlawva.domain.alarm.dto;

import com.superlawva.domain.alarm.entity.AlarmType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "알림 응답 DTO")
public class AlarmResponseDTO {
    
    @Schema(description = "알림 ID", example = "1")
    private Long alarmId;
    
    @Schema(description = "계약서 ID", example = "contract_123")
    private String contractId;
    
    @Schema(description = "알림 타입", example = "PAYMENT_DUE")
    private AlarmType alarmType;
    
    @Schema(description = "알림 메시지", example = "보증금 납부일이 다가왔습니다.")
    private String note;
    
    @Schema(description = "추가 정보", example = "납부 금액: 5000만원")
    private String extraInfo;
    
    @Schema(description = "읽음 여부", example = "false")
    private boolean isRead;
    
    @Schema(description = "알림 예정일", example = "2025-01-15T10:30:00")
    private LocalDateTime alarmDate;
    
    @Schema(description = "생성일시", example = "2025-01-10T10:30:00")
    private LocalDateTime createdAt;
    
    public Long getAlarmId() {
        return alarmId;
    }
    
    public String getContractId() {
        return contractId;
    }
    
    public AlarmType getAlarmType() {
        return alarmType;
    }
    
    public String getNote() {
        return note;
    }
    
    public String getExtraInfo() {
        return extraInfo;
    }
    
    public boolean isRead() {
        return isRead;
    }
    
    public LocalDateTime getAlarmDate() {
        return alarmDate;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
} 