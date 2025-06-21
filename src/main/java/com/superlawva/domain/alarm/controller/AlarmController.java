// AlarmController.java - 수정됨: 닫히지 않은 메서드 수정
package com.superlawva.domain.alarm.controller;

import com.superlawva.domain.alarm.dto.AlarmRequestDTO;
import com.superlawva.domain.alarm.dto.AlarmResponseDTO;
import com.superlawva.domain.alarm.service.AlarmService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/alarms") // 알람 API 기본 경로 설정
@RequiredArgsConstructor // Lombok을 사용한 의존성 주입
@Tag(name = "Alarm API", description = "계약 알림 관련 API") // Swagger 문서화를 위한 태그
public class AlarmController {

    private final AlarmService alarmService; // 알람 서비스 의존성 주입

    /**
     * 사용자 ID로 읽지 않은 알람 목록 조회
     */
    @Operation(summary = "알람 목록 조회", description = "특정 사용자에 대한 읽지 않은 알람을 조회합니다.")
    @GetMapping("/user/{userId}") // GET 요청 매핑
    public ResponseEntity<List<AlarmResponseDTO>> getAlarms(@PathVariable Long userId) {
        List<AlarmResponseDTO> alarms = alarmService.getUnreadAlarms(userId); // 읽지 않은 알람 조회
        return ResponseEntity.ok(alarms); // 200 OK와 함께 알람 목록 반환
    }

    /**
     * 알람 생성
     */
    @Operation(summary = "알람 생성", description = "단일 알람을 수동으로 생성합니다.")
    @PostMapping // POST 요청 매핑
    public ResponseEntity<Map<String, Object>> createAlarm(@RequestBody AlarmRequestDTO dto) {
        alarmService.createAlarm(dto); // 알람 생성 서비스 호출
        return ResponseEntity.ok(Map.of("message", "알림이 생성되었습니다.")); // 성공 메시지 반환
    }

    /**
     * 알람 읽음 처리
     */
    @Operation(summary = "알람 읽음 처리", description = "알람을 읽음 상태로 변경합니다.")
    @PutMapping("/{alarmId}/read") // PUT 요청 매핑
    public ResponseEntity<Map<String, Object>> markAsRead(@PathVariable Long alarmId) {
        alarmService.markAsRead(alarmId); // 알람 읽음 처리 서비스 호출
        return ResponseEntity.ok(Map.of("message", "알림을 읽음 처리했습니다.")); // 성공 메시지 반환
    }

    /**
     * 알람 삭제 처리 (소프트 딜리트)
     */
    @Operation(summary = "알람 삭제", description = "알림을 삭제합니다.")
    @DeleteMapping("/{alarmId}") // DELETE 요청 매핑
    public ResponseEntity<Map<String, Object>> deleteAlarm(@PathVariable Long alarmId) {
        alarmService.deleteAlarm(alarmId); // 알람 삭제 서비스 호출
        return ResponseEntity.ok(Map.of("message", "알림이 삭제되었습니다.")); // 성공 메시지 반환
    }
}