// AlarmEntity.java - 수정 없음, 주석 추가
package com.superlawva.domain.alarm.entity;

import com.superlawva.domain.alarm.enums.AlarmType;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * 알람 엔티티 클래스
 */
@Entity
@Table(name = "contract_alarm") // 테이블 이름 지정
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlarmEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 자동 증가 ID
    private Long alarmId; // 알람 고유 ID

    private Long userId; // 사용자 ID

    @Column(name = "contract_id") // 컬럼 이름 지정
    private String contractId; // MongoDB의 _id와 매핑 (String 타입)

    @Enumerated(EnumType.STRING) // Enum을 문자열로 저장
    private AlarmType alarmType; // 알람 유형

    private String note; // 알림 메시지 내용

    @Column(columnDefinition = "TEXT") // TEXT 타입 컬럼
    private String extraInfo; // JSON 형태의 추가 정보

    private boolean isRead; // 읽음 여부

    private LocalDateTime alarmDate; // 알림 발생 예정 시각

    private LocalDateTime createdAt; // 생성 시각

    private LocalDateTime deletedAt; // 소프트 삭제용 시각

    private boolean isSent; // 발송 여부

    private LocalDateTime sentAt; // 발송 시각
}