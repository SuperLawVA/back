package com.superlawva.domain.alarm.controller;

import com.superlawva.domain.alarm.dto.AlarmMessageResponseDTO;
import com.superlawva.domain.alarm.dto.AlarmRequestDTO;
import com.superlawva.domain.alarm.dto.AlarmResponseDTO;
import com.superlawva.domain.alarm.dto.AlarmStatsDTO;
import com.superlawva.domain.alarm.service.AlarmService;
import com.superlawva.domain.user.entity.User;
import com.superlawva.global.exception.BaseException;
import com.superlawva.global.response.ApiResponse;
import com.superlawva.global.response.status.ErrorStatus;
import com.superlawva.global.response.status.SuccessStatus;
import com.superlawva.global.security.annotation.LoginUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/alarms")
@RequiredArgsConstructor
@Tag(name = "🔔 Alarm Management", description = "알림 관리 API")
public class AlarmController {
    private final AlarmService alarmService;

    @Operation(summary = "내 알람 목록 조회", description = "현재 로그인한 사용자의 읽지 않은 알람을 조회합니다.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "알람 목록 조회 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponse.class),
                examples = @ExampleObject(
                    name = "성공 응답",
                    value = """
                    {
                        "success": true,
                        "message": "알람 목록을 성공적으로 조회했습니다.",
                        "data": [
                            {
                                "id": 1,
                                "title": "계약서 분석 완료",
                                "content": "업로드하신 계약서 분석이 완료되었습니다.",
                                "type": "CONTRACT_ANALYSIS",
                                "isRead": false,
                                "createdAt": "2024-01-15T10:30:00"
                            },
                            {
                                "id": 2,
                                "title": "새로운 판례 발견",
                                "content": "관련 판례가 새로 등록되었습니다.",
                                "type": "NEW_CASE",
                                "isRead": false,
                                "createdAt": "2024-01-15T09:15:00"
                            }
                        ]
                    }
                    """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401", 
            description = "인증되지 않은 사용자 (JWT 토큰 없음 또는 만료)",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponse.class),
                examples = @ExampleObject(
                    name = "인증 실패",
                    value = """
                    {
                        "success": false,
                        "message": "인증되지 않은 사용자입니다.",
                        "errorCode": "UNAUTHORIZED"
                    }
                    """
                )
            )
        )
    })
    @GetMapping
    @SecurityRequirement(name = "JWT")
    public ResponseEntity<ApiResponse<List<AlarmResponseDTO>>> getMyAlarms(@Parameter(hidden = true) @LoginUser User user) {
        if (user == null) throw new BaseException(ErrorStatus.UNAUTHORIZED);
        List<AlarmResponseDTO> alarms = alarmService.getUnreadAlarms(user.getId());
        return ResponseEntity.ok(ApiResponse.of(SuccessStatus.ALARM_LIST_RETRIEVED, alarms));
    }

    @Operation(summary = "내 알람 통계 조회", description = "현재 로그인한 사용자의 알람 통계 정보를 조회합니다.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "알람 통계 조회 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponse.class),
                examples = @ExampleObject(
                    name = "성공 응답",
                    value = """
                    {
                        "success": true,
                        "message": "알람 통계를 성공적으로 조회했습니다.",
                        "data": {
                            "totalAlarms": 15,
                            "unreadAlarms": 3,
                            "readAlarms": 12,
                            "todayAlarms": 2
                        }
                    }
                    """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401", 
            description = "인증되지 않은 사용자 (JWT 토큰 없음 또는 만료)",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponse.class),
                examples = @ExampleObject(
                    name = "인증 실패",
                    value = """
                    {
                        "success": false,
                        "message": "인증되지 않은 사용자입니다.",
                        "errorCode": "UNAUTHORIZED"
                    }
                    """
                )
            )
        )
    })
    @GetMapping("/stats")
    @SecurityRequirement(name = "JWT")
    public ResponseEntity<ApiResponse<AlarmStatsDTO>> getMyAlarmStats(@Parameter(hidden = true) @LoginUser User user) {
        if (user == null) throw new BaseException(ErrorStatus.UNAUTHORIZED);
        AlarmStatsDTO stats = alarmService.getAlarmStats(user.getId());
        return ResponseEntity.ok(ApiResponse.of(SuccessStatus.ALARM_STATS_RETRIEVED, stats));
    }

    @Operation(summary = "모든 알람 읽음 처리", description = "현재 로그인한 사용자의 모든 알람을 읽음 상태로 변경합니다.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "알람 읽음 처리 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponse.class),
                examples = @ExampleObject(
                    name = "성공 응답",
                    value = """
                    {
                        "success": true,
                        "message": "모든 알림을 읽음 처리했습니다.",
                        "data": {
                            "message": "모든 알림을 읽음 처리했습니다.",
                            "processedCount": 5,
                            "processedAt": "2024-01-15T10:30:00"
                        }
                    }
                    """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401", 
            description = "인증되지 않은 사용자 (JWT 토큰 없음 또는 만료)",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponse.class),
                examples = @ExampleObject(
                    name = "인증 실패",
                    value = """
                    {
                        "success": false,
                        "message": "인증되지 않은 사용자입니다.",
                        "errorCode": "UNAUTHORIZED"
                    }
                    """
                )
            )
        )
    })
    @PutMapping("/read-all")
    @SecurityRequirement(name = "JWT")
    public ResponseEntity<ApiResponse<AlarmMessageResponseDTO>> markAllAsRead(@Parameter(hidden = true) @LoginUser User user) {
        if (user == null) throw new BaseException(ErrorStatus.UNAUTHORIZED);
        int processedCount = alarmService.markAllAsRead(user.getId());
        AlarmMessageResponseDTO response = AlarmMessageResponseDTO.builder()
            .message("모든 알림을 읽음 처리했습니다.")
            .processedCount(processedCount)
            .processedAt(LocalDateTime.now().toString())
            .build();
        return ResponseEntity.ok(ApiResponse.of(SuccessStatus.ALARM_MARKED_AS_READ, response));
    }

    @Operation(summary = "알람 읽음 처리", description = "특정 알람을 읽음 상태로 변경합니다.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "알람 읽음 처리 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponse.class),
                examples = @ExampleObject(
                    name = "성공 응답",
                    value = """
                    {
                        "success": true,
                        "message": "알림을 읽음 처리했습니다.",
                        "data": {
                            "message": "알림을 읽음 처리했습니다.",
                            "processedCount": 1,
                            "processedAt": "2024-01-15T10:30:00"
                        }
                    }
                    """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401", 
            description = "인증되지 않은 사용자 (JWT 토큰 없음 또는 만료)",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponse.class),
                examples = @ExampleObject(
                    name = "인증 실패",
                    value = """
                    {
                        "success": false,
                        "message": "인증되지 않은 사용자입니다.",
                        "errorCode": "UNAUTHORIZED"
                    }
                    """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404", 
            description = "알람을 찾을 수 없음",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponse.class),
                examples = @ExampleObject(
                    name = "알람 없음",
                    value = """
                    {
                        "success": false,
                        "message": "알람을 찾을 수 없습니다.",
                        "errorCode": "ALARM_NOT_FOUND"
                    }
                    """
                )
            )
        )
    })
    @PutMapping("/{alarmId}/read")
    @SecurityRequirement(name = "JWT")
    public ResponseEntity<ApiResponse<AlarmMessageResponseDTO>> markAsRead(@PathVariable Long alarmId) {
        alarmService.markAsRead(alarmId);
        AlarmMessageResponseDTO response = AlarmMessageResponseDTO.builder()
            .message("알림을 읽음 처리했습니다.")
            .processedCount(1)
            .processedAt(LocalDateTime.now().toString())
            .build();
        return ResponseEntity.ok(ApiResponse.of(SuccessStatus.ALARM_MARKED_AS_READ, response));
    }

    @Operation(summary = "알람 삭제", description = "특정 알림을 삭제합니다.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "알람 삭제 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponse.class),
                examples = @ExampleObject(
                    name = "성공 응답",
                    value = """
                    {
                        "success": true,
                        "message": "알림이 삭제되었습니다.",
                        "data": {
                            "message": "알림이 삭제되었습니다.",
                            "processedCount": 1,
                            "processedAt": "2024-01-15T10:30:00"
                        }
                    }
                    """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401", 
            description = "인증되지 않은 사용자 (JWT 토큰 없음 또는 만료)",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponse.class),
                examples = @ExampleObject(
                    name = "인증 실패",
                    value = """
                    {
                        "success": false,
                        "message": "인증되지 않은 사용자입니다.",
                        "errorCode": "UNAUTHORIZED"
                    }
                    """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404", 
            description = "알람을 찾을 수 없음",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponse.class),
                examples = @ExampleObject(
                    name = "알람 없음",
                    value = """
                    {
                        "success": false,
                        "message": "알람을 찾을 수 없습니다.",
                        "errorCode": "ALARM_NOT_FOUND"
                    }
                    """
                )
            )
        )
    })
    @DeleteMapping("/{alarmId}")
    @SecurityRequirement(name = "JWT")
    public ResponseEntity<ApiResponse<AlarmMessageResponseDTO>> deleteAlarm(@PathVariable Long alarmId) {
        alarmService.deleteAlarm(alarmId);
        AlarmMessageResponseDTO response = AlarmMessageResponseDTO.builder()
            .message("알림이 삭제되었습니다.")
            .processedCount(1)
            .processedAt(LocalDateTime.now().toString())
            .build();
        return ResponseEntity.ok(ApiResponse.of(SuccessStatus.ALARM_DELETED, response));
    }
    
    // ================================================================================================================
    // 아래 API는 관리자 또는 시스템 내부용입니다.
    // ================================================================================================================

    @Operation(summary = "알람 수동 생성 (관리자/시스템용)", description = "단일 알람을 수동으로 생성합니다.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "알람 생성 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponse.class),
                examples = @ExampleObject(
                    name = "성공 응답",
                    value = """
                    {
                        "success": true,
                        "message": "알림이 생성되었습니다.",
                        "data": {
                            "message": "알림이 생성되었습니다.",
                            "processedCount": 1,
                            "processedAt": "2024-01-15T10:30:00"
                        }
                    }
                    """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401", 
            description = "인증되지 않은 사용자 (JWT 토큰 없음 또는 만료)",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponse.class),
                examples = @ExampleObject(
                    name = "인증 실패",
                    value = """
                    {
                        "success": false,
                        "message": "인증되지 않은 사용자입니다.",
                        "errorCode": "UNAUTHORIZED"
                    }
                    """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400", 
            description = "잘못된 요청 데이터",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponse.class),
                examples = @ExampleObject(
                    name = "잘못된 요청",
                    value = """
                    {
                        "success": false,
                        "message": "잘못된 요청 데이터입니다.",
                        "errorCode": "INVALID_REQUEST"
                    }
                    """
                )
            )
        )
    })
    @PostMapping
    @SecurityRequirement(name = "JWT")
    public ResponseEntity<ApiResponse<AlarmMessageResponseDTO>> createAlarm(@RequestBody AlarmRequestDTO dto) {
        alarmService.createAlarm(dto);
        AlarmMessageResponseDTO response = AlarmMessageResponseDTO.builder()
            .message("알림이 생성되었습니다.")
            .processedCount(1)
            .processedAt(LocalDateTime.now().toString())
            .build();
        return ResponseEntity.ok(ApiResponse.of(SuccessStatus.ALARM_CREATED, response));
    }

    @PutMapping("/{id}/read")
    @Operation(summary = "알림 읽음 처리", description = "특정 알림을 읽음 상태로 변경합니다.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "알림 읽음 처리 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponse.class),
                examples = @ExampleObject(
                    name = "성공 응답",
                    value = """
                    {
                        "success": true,
                        "message": "알림을 읽음 처리했습니다.",
                        "data": {
                            "message": "알림을 읽음 처리했습니다.",
                            "processedCount": 1,
                            "processedAt": "2024-01-15T10:30:00"
                        }
                    }
                    """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401", 
            description = "인증 필요",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponse.class),
                examples = @ExampleObject(
                    name = "인증 실패",
                    value = """
                    {
                        "success": false,
                        "message": "인증되지 않은 사용자입니다.",
                        "errorCode": "UNAUTHORIZED"
                    }
                    """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404", 
            description = "알림을 찾을 수 없음",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponse.class),
                examples = @ExampleObject(
                    name = "알림 없음",
                    value = """
                    {
                        "success": false,
                        "message": "알림을 찾을 수 없습니다.",
                        "errorCode": "ALARM_NOT_FOUND"
                    }
                    """
                )
            )
        )
    })
    @SecurityRequirement(name = "JWT")
    public ResponseEntity<ApiResponse<AlarmMessageResponseDTO>> markAlarmAsRead(
            @Parameter(description = "알림 ID") @PathVariable Long id,
            @Parameter(hidden = true) @LoginUser User user) {
        if (user == null) throw new BaseException(ErrorStatus.UNAUTHORIZED);
        alarmService.markAsRead(id);
        AlarmMessageResponseDTO response = AlarmMessageResponseDTO.builder()
            .message("알림을 읽음 처리했습니다.")
            .processedCount(1)
            .processedAt(LocalDateTime.now().toString())
            .build();
        return ResponseEntity.ok(ApiResponse.of(SuccessStatus.ALARM_MARKED_AS_READ, response));
    }
} 