package com.superlawva.domain.status.controller;

import com.superlawva.domain.status.dto.StatusResponseDTO;
import com.superlawva.domain.status.service.StatusService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "📊 Status API", description = "ML 서비스 상태 확인 API")
public class StatusController {
    
    private final StatusService statusService;
    
    @Operation(
        summary = "📊 ML 서비스 상태 확인", 
        description = """
        ##그냥 구현만 해놓은 것 사용 x##
        ## 📖 API 설명
        ML 팀의 AI 서버 상태를 실시간으로 확인합니다.
        
        ## 🎯 프론트엔드 구현 가이드
        
        ### 1. 요청 방법
        ```javascript
        fetch('/api/v1/status', {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json'
            }
        })
        .then(response => {
            if (response.ok) {
                return response.json();
            } else if (response.status === 503) {
                throw new Error('서비스 일시 중단');
            }
        })
        .then(data => {
            updateStatusUI(data);
        })
        .catch(error => {
            showServiceDownMessage();
        });
        ```
        
        ### 2. 상태별 UI 처리
        ```javascript
        function updateStatusUI(status) {
            switch(status.status) {
                case 'running':
                    showGreenIndicator('서비스 정상 운영중');
                    enableChatButton();
                    break;
                case 'degraded':
                    showYellowIndicator('일부 기능 제한');
                    showPerformanceWarning();
                    break;
                default:
                    showRedIndicator('서비스 점검중');
                    disableChatButton();
            }
        }
        ```
        
        ### 3. 주기적 상태 확인
        ```javascript
        // 30초마다 상태 확인
        setInterval(() => {
            checkServiceStatus();
        }, 30000);
        ```
        
        ### 4. 확인 항목 설명
        - **status**: 전체 서비스 상태 (`running`, `degraded`, `down`)
        - **active_sessions**: 현재 활성 대화 세션 수
        - **models**: AI 모델 상태 (light/heavy)
        - **vector_search_ready**: 검색 기능 사용 가능 여부
        
        ### 5. 에러 처리
        - **200**: 서비스 정상 → 챗봇/검색 기능 사용 가능
        - **503**: 서비스 장애 → 사용자에게 점검 안내
        """
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "✅ 상태 확인 성공",
            content = @Content(
                examples = @ExampleObject(
                    value = """
                    {
                        "status": "running",
                        "active_sessions": 42,
                        "models": {
                            "light": "healthy",
                            "heavy": "healthy",
                            "last_health_check": "2025-06-20T15:49:00"
                        },
                        "vector_search_ready": true,
                        "timestamp": "2025-06-20T15:49:27.843808"
                    }
                    """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "503", 
            description = "⚠️ 서비스 장애",
            content = @Content(
                examples = @ExampleObject(
                    value = """
                    {
                        "status": "degraded",
                        "active_sessions": 15,
                        "models": {
                            "light": "warning",
                            "heavy": "warning",
                            "last_health_check": "2025-06-20T15:49:00"
                        },
                        "vector_search_ready": false,
                        "timestamp": "2025-06-20T15:49:27.843808"
                    }
                    """
                )
            )
        )
    })
    @GetMapping("/status")
    public ResponseEntity<StatusResponseDTO> getStatus() {
        log.info("ML 서비스 상태 확인 요청");
        
        StatusResponseDTO status = statusService.getStatus();
        
        // 상태에 따른 HTTP 상태 코드 반환
        if ("running".equals(status.status())) {
            return ResponseEntity.ok(status);
        } else if ("degraded".equals(status.status())) {
            return ResponseEntity.status(503).body(status); // Service Unavailable
        } else {
            return ResponseEntity.status(503).body(status); // Service Unavailable
        }
    }
} 