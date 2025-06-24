package com.superlawva.domain.alarm.controller;

import com.superlawva.domain.alarm.dto.AlarmRequestDTO;
import com.superlawva.domain.alarm.dto.AlarmResponseDTO;
import com.superlawva.domain.alarm.dto.AlarmStatsDTO;
import com.superlawva.domain.alarm.service.AlarmService;
import com.superlawva.domain.user.entity.User;
import com.superlawva.global.exception.BaseException;
import com.superlawva.global.response.status.ErrorStatus;
import com.superlawva.global.security.annotation.LoginUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/alarms")
@RequiredArgsConstructor
@Tag(name = "🔔 Alarm Management", description = "계약 알림 관리 API (🔒인증 필요)")
public class AlarmController {
    private final AlarmService alarmService;

    @Operation(summary = "내 알람 목록 조회", description = "현재 로그인한 사용자의 읽지 않은 알람을 조회합니다.")
    @GetMapping
    @SecurityRequirement(name = "JWT")
    public ResponseEntity<List<AlarmResponseDTO>> getMyAlarms(@Parameter(hidden = true) @LoginUser User user) {
        if (user == null) throw new BaseException(ErrorStatus._UNAUTHORIZED);
        List<AlarmResponseDTO> alarms = alarmService.getUnreadAlarms(user.getId());
        return ResponseEntity.ok(alarms);
    }

    @Operation(summary = "내 알람 통계 조회", description = "현재 로그인한 사용자의 알람 통계 정보를 조회합니다.")
    @GetMapping("/stats")
    @SecurityRequirement(name = "JWT")
    public ResponseEntity<AlarmStatsDTO> getMyAlarmStats(@Parameter(hidden = true) @LoginUser User user) {
        if (user == null) throw new BaseException(ErrorStatus._UNAUTHORIZED);
        AlarmStatsDTO stats = alarmService.getAlarmStats(user.getId());
        return ResponseEntity.ok(stats);
    }

    @Operation(summary = "모든 알람 읽음 처리", description = "현재 로그인한 사용자의 모든 알람을 읽음 상태로 변경합니다.")
    @PutMapping("/read-all")
    @SecurityRequirement(name = "JWT")
    public ResponseEntity<Map<String, Object>> markAllAsRead(@Parameter(hidden = true) @LoginUser User user) {
        if (user == null) throw new BaseException(ErrorStatus._UNAUTHORIZED);
        alarmService.markAllAsRead(user.getId());
        return ResponseEntity.ok(Map.of("message", "모든 알림을 읽음 처리했습니다."));
    }

    @Operation(summary = "알람 읽음 처리", description = "특정 알람을 읽음 상태로 변경합니다.")
    @PutMapping("/{alarmId}/read")
    @SecurityRequirement(name = "JWT")
    public ResponseEntity<Map<String, Object>> markAsRead(@PathVariable Long alarmId) {
        alarmService.markAsRead(alarmId);
        return ResponseEntity.ok(Map.of("message", "알림을 읽음 처리했습니다."));
    }

    @Operation(summary = "알람 삭제", description = "특정 알림을 삭제합니다.")
    @DeleteMapping("/{alarmId}")
    @SecurityRequirement(name = "JWT")
    public ResponseEntity<Map<String, Object>> deleteAlarm(@PathVariable Long alarmId) {
        alarmService.deleteAlarm(alarmId);
        return ResponseEntity.ok(Map.of("message", "알림이 삭제되었습니다."));
    }
    
    // ================================================================================================================
    // 아래 API는 관리자 또는 시스템 내부용입니다.
    // ================================================================================================================

    @Operation(summary = "알람 수동 생성 (관리자/시스템용)", description = "단일 알람을 수동으로 생성합니다.")
    @PostMapping
    @SecurityRequirement(name = "JWT")
    public ResponseEntity<Map<String, Object>> createAlarm(@RequestBody AlarmRequestDTO dto) {
        alarmService.createAlarm(dto);
        return ResponseEntity.ok(Map.of("message", "알림이 생성되었습니다."));
    }
} 