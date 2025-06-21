// AlarmRepository.java - 수정 없음, 주석 추가
package com.superlawva.domain.alarm.repository;

import com.superlawva.domain.alarm.entity.AlarmEntity;
import com.superlawva.domain.alarm.enums.AlarmType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 알람 엔티티 레포지토리
 */
public interface AlarmRepository extends JpaRepository<AlarmEntity, Long> {

    /**
     * 사용자의 읽지 않은 알람 조회 (삭제되지 않은 것만)
     */
    List<AlarmEntity> findByUserIdAndIsReadFalseAndDeletedAtIsNull(Long userId);

    /**
     * 특정 계약의 특정 타입 알람 존재 여부 확인 (중복 방지용)
     */
    boolean existsByContractIdAndAlarmTypeAndDeletedAtIsNull(String contractId, AlarmType alarmType);

    /**
     * 발송 예정 알람 조회 (스케줄러용)
     */
    @Query("SELECT ca FROM AlarmEntity ca WHERE ca.alarmDate <= :now AND ca.isSent = false AND ca.deletedAt IS NULL")
    List<AlarmEntity> findAlarmsToSend(@Param("now") LocalDateTime now);

    /**
     * 특정 기간의 알람 조회
     */
    @Query("SELECT ca FROM AlarmEntity ca WHERE ca.alarmDate BETWEEN :start AND :end AND ca.deletedAt IS NULL")
    List<AlarmEntity> findAlarmsByDateRange(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}