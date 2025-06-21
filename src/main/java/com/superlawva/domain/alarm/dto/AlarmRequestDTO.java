// AlarmRequestDTO.java - 수정 없음, 주석 추가
package com.superlawva.domain.alarm.dto;

import com.superlawva.domain.alarm.enums.AlarmType;
import lombok.*;
import java.time.LocalDateTime;

/**
 * 알람 생성 요청 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlarmRequestDTO {
    private Long userId; // 사용자 ID
    private String contractId; // 계약 ID (MongoDB _id 대응)
    private AlarmType alarmType; // 알람 유형
    private String extraInfo; // 추가 정보 (JSON 형태)
    private LocalDateTime alarmDate; // 알람 발생 예정 시각
}