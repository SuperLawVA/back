package com.superlawva.domain.alarm.dto;

import lombok.*;

/**
 * 알람 통계 정보 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlarmStatsDTO {
    private Long userId;
    private int totalAlarms;
    private int unreadAlarms;
    private int urgentAlarms;
    private int todayAlarms;
    private int weekAlarms;
    private int monthAlarms;
    
    // 명시적으로 필요한 getter 메서드들 추가
    public Long getUserId() {
        return userId;
    }
    
    public int getTotalAlarms() {
        return totalAlarms;
    }
    
    public int getUnreadAlarms() {
        return unreadAlarms;
    }
    
    public int getUrgentAlarms() {
        return urgentAlarms;
    }
    
    public int getTodayAlarms() {
        return todayAlarms;
    }
    
    public int getWeekAlarms() {
        return weekAlarms;
    }
    
    public int getMonthAlarms() {
        return monthAlarms;
    }
} 