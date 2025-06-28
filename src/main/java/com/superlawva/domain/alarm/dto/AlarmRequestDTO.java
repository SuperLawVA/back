package com.superlawva.domain.alarm.dto;

import com.superlawva.domain.alarm.entity.AlarmType;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlarmRequestDTO {
    private Long userId;
    private String contractId;
    private AlarmType alarmType;
    private String extraInfo;
    private LocalDateTime alarmDate;
    
    public Long getUserId() {
        return userId;
    }
    
    public String getContractId() {
        return contractId;
    }
    
    public AlarmType getAlarmType() {
        return alarmType;
    }
    
    public String getExtraInfo() {
        return extraInfo;
    }
    
    public LocalDateTime getAlarmDate() {
        return alarmDate;
    }
} 