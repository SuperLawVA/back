package com.superlawva.domain.status.controller;

import com.superlawva.domain.status.dto.StatusResponseDTO;
import com.superlawva.domain.status.service.StatusService;
import com.superlawva.global.response.ApiResponse;
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
@RequestMapping("/api/v1/status")
@RequiredArgsConstructor
@Tag(name = "📊 System Status", description = "시스템 상태 및 모니터링 API")
public class StatusController {
    
    private final StatusService statusService;
    
    @Operation(
        summary = "📊 시스템 상태 확인", 
        description = """
        ## 📖 API 설명
        시스템의 전반적인 상태를 확인합니다.
        - ML 서비스 상태
        - 활성 세션 수
        - 벡터 검색 준비 상태
        
        ### 응답 상태
        - **running**: 정상 작동
        - **degraded**: 일부 기능 제한
        - **down**: 서비스 불가
        """
    )
    @GetMapping
    public ResponseEntity<ApiResponse<?>> getStatus() {
        log.info("시스템 상태 확인 요청");
        
        StatusResponseDTO status = statusService.getStatus();
        
        // 상태에 따른 HTTP 상태 코드 반환
        if ("running".equals(status.status())) {
            return ResponseEntity.ok(ApiResponse.success(status));
        } else if ("degraded".equals(status.status())) {
            return ResponseEntity.status(503).body(ApiResponse.onFailure("STATUS503", "서비스가 일부 제한됩니다", null));
        } else {
            return ResponseEntity.status(503).body(ApiResponse.onFailure("STATUS503", "서비스를 이용할 수 없습니다", null));
        }
    }
} 