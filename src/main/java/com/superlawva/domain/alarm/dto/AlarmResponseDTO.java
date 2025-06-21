// AlarmResponseDTO.java - 수정됨: contractId 필드 추가
package com.superlawva.domain.alarm.dto;

import com.superlawva.domain.alarm.enums.AlarmType;
import lombok.*;
import java.time.LocalDateTime;

/**
 * 알람 응답 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlarmResponseDTO {
    private Long alarmId; // 알람 ID
    private String contractId; // 계약 ID
    private AlarmType alarmType; // 알람 유형
    private String note; // 알람 메시지 내용
    private String extraInfo; // 추가 정보 (JSON 형태)
    private boolean isRead; // 읽음 여부
    private LocalDateTime alarmDate; // 알람 발생 예정 시각
    private LocalDateTime createdAt; // 생성 시각
}