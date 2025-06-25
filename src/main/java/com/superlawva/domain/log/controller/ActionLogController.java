package com.superlawva.domain.log.controller;

import com.superlawva.domain.log.dto.EventRequestDTO;
import com.superlawva.domain.log.dto.PageViewRequestDTO;
import com.superlawva.domain.log.dto.SessionRequestDTO;
import com.superlawva.domain.log.service.LogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/log")
@RequiredArgsConstructor
@Tag(name = "📊 User Behavior Logging", description = "사용자 행동 로깅 API")
public class ActionLogController {

    private final LogService logService;

    /* ===== 세션 로그 ===== */
    @Operation(
        summary = "📱 세션 시작/종료",
        description = """
        사용자 세션의 시작과 종료를 추적합니다.
        
        **사용법:**
        - **세션 시작**: `action: "start"` → 새로운 세션 생성
        - **세션 종료**: `action: "end"` → 기존 세션 종료
        
        **요청 예시:**
        ```json
        {
            "action": "start",
            "sessionId": null,
            "userId": 1,
            "ip": "192.168.1.1",
            "userAgent": "Mozilla/5.0...",
            "timestamp": "2025-01-20T10:30:00"
        }
        ```
        
        **응답:**
        - **시작**: 201 Created + `{"session_id": 123}`
        - **종료**: 204 No Content
        
        **활용:**
        - 사용자 접속 시간 분석
        - 세션 유지 시간 측정
        - 사용자 활동 패턴 파악
        """
    )
    @PostMapping("/session")
    public ResponseEntity<?> session(@RequestBody SessionRequestDTO req) {
        Long sessionId = logService.handleSession(req);

        if ("start".equals(req.action())) {
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(Map.of("session_id", sessionId));
        } else {
            return ResponseEntity.noContent().build();
        }
    }

    /* ===== 페이지뷰 로그 ===== */
    @Operation(
        summary = "🌐 페이지뷰 시작/종료",
        description = """
        사용자가 페이지에 진입하고 이탈하는 것을 추적합니다.
        
        **사용법:**
        - **페이지 진입**: `action: "start"` → 새로운 페이지뷰 생성
        - **페이지 이탈**: `action: "end"` → 기존 페이지뷰 종료
        
        **요청 예시:**
        ```json
        {
            "action": "start",
            "sessionId": 123,
            "userId": 1,
            "url": "/dashboard",
            "title": "대시보드",
            "referrer": "/login",
            "timestamp": "2025-01-20T10:35:00"
        }
        ```
        
        **응답:**
        - **시작**: 201 Created + `{"view_id": 456}`
        - **종료**: 204 No Content
        
        **활용:**
        - 페이지별 체류 시간 측정
        - 인기 페이지 분석
        - 사용자 이동 경로 추적
        """
    )
    @PostMapping("/pageview")
    public ResponseEntity<?> pageview(@RequestBody PageViewRequestDTO req) {
        Long viewId = logService.handlePageView(req);

        if ("start".equals(req.action())) {
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(Map.of("view_id", viewId));
        } else {
            return ResponseEntity.noContent().build();
        }
    }

    /* ===== 이벤트 로그 ===== */
    @Operation(
        summary = "🎯 사용자 이벤트 로그 생성",
        description = """
        사용자의 다양한 행동 이벤트를 추적합니다.
        
        **지원하는 이벤트 타입:**
        - `CLICK` - 클릭 이벤트
        - `HOVER` - 마우스 호버 이벤트
        - `SCROLL` - 스크롤 이벤트
        - `INPUT` - 입력 이벤트
        - `FORM` - 폼 제출 이벤트
        - `NAVI` - 네비게이션 이벤트
        - `ERROR` - 에러 이벤트
        
        **요청 예시:**
        ```json
        {
            "sessionId": 123,
            "viewId": 456,
            "userId": 1,
            "type": "CLICK",
            "meta": {
                "elementId": "signup-button",
                "elementText": "회원가입",
                "x": 150,
                "y": 300
            },
            "timestamp": "2025-01-20T10:40:00"
        }
        ```
        
        **응답:**
        - 201 Created + `{"event_id": 789}`
        
        **활용:**
        - 사용자 행동 패턴 분석
        - UI/UX 개선 데이터 수집
        - A/B 테스트 결과 측정
        - 에러 추적 및 개선
        """
    )
    @PostMapping("/event")
    public ResponseEntity<?> event(@RequestBody EventRequestDTO req) {
        Long eventId = logService.handleEvent(req);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(Map.of("event_id", eventId));
    }
}
