package com.superlawva.domain.ml.controller;

import com.superlawva.domain.ml.dto.MLAnalysisRequest;
import com.superlawva.domain.ml.service.MLAnalysisService;
import com.superlawva.domain.ml.entity.MLAnalysisResult;
import com.superlawva.domain.ml.entity.CertificateEntity;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.superlawva.global.response.ApiResponse;
import com.superlawva.global.response.ErrorResponse;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Slf4j
@RestController
@RequestMapping("/analysis")
@RequiredArgsConstructor
@Tag(name = "🤖 ML Analysis", description = "계약서 AI 분석 및 인증서 관리 API")
public class MLAnalysisController {

    private final MLAnalysisService mlAnalysisService;

    @PostMapping
    @Operation(
        summary = "🤖 AI 계약서 분석 시작", 
        description = """
        선택한 계약서에 대해 AI 분석을 시작합니다.
        
        ## 🎯 프론트엔드 구현 가이드
        
        ### 1. 기본 사용법
        ```javascript
        const startAnalysis = async (analysisData) => {
            const response = await fetch('/analysis', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${token}`
                },
                body: JSON.stringify({
                    contractId: "123",
                    analysisType: "COMPREHENSIVE",
                    focusAreas: ["보증금 관련", "계약 기간"],
                    options: {
                        includeRecommendations: true,
                        includeLegalBasis: true,
                        detailLevel: "DETAILED"
                    }
                })
            });
            return await response.json();
        };
        ```
        
        ### 2. 분석 유형 (analysisType)
        - `RISK_ANALYSIS`: 위험도 중심 분석
        - `COMPLIANCE_CHECK`: 법적 준수성 검토
        - `COMPREHENSIVE`: 종합 분석 (추천)
        - `QUICK_REVIEW`: 빠른 검토
        
        ### 3. 상세도 레벨 (detailLevel)
        - `SUMMARY`: 요약
        - `DETAILED`: 상세 (기본값)
        - `EXPERT`: 전문가 수준
        """
    )
    @io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "JWT")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "✅ 분석 시작 성공",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "분석 시작 성공 예시",
                    summary = "분석이 성공적으로 시작됨",
                    value = """
                    {
                        "success": true,
                        "data": {
                            "analysisId": "456",
                            "contractId": "123",
                            "analysisType": "COMPREHENSIVE",
                            "status": "PROCESSING",
                            "estimatedTime": "2-3분",
                            "startedAt": "2024-01-15T10:30:00Z"
                        },
                        "message": "계약서 분석이 시작되었습니다."
                    }
                    """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "❌ 잘못된 요청",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "유효성 검증 실패 예시",
                    summary = "필수 필드 누락 또는 형식 오류",
                    value = """
                    {
                        "success": false,
                        "error": {
                            "code": "VALIDATION_ERROR",
                            "message": "입력값 검증에 실패했습니다.",
                            "details": {
                                "contractId": "계약서 ID는 필수입니다.",
                                "analysisType": "분석 유형은 필수입니다."
                            }
                        }
                    }
                    """
                )
            )
        )
    })
    public ResponseEntity<ApiResponse<Map<String, Object>>> createAnalysis(
            @Parameter(hidden = true) @com.superlawva.global.security.annotation.LoginUser Long userId,
            @Valid @RequestBody MLAnalysisRequest request) {
        log.info("🤖 AI 계약서 분석 시작 요청 - User ID: {}, Contract ID: {}, Type: {}", 
                userId, request.getContractId(), request.getAnalysisType());
                
        if (userId == null) {
            throw new com.superlawva.global.exception.BaseException(
                com.superlawva.global.response.status.ErrorStatus.UNAUTHORIZED, 
                "인증이 필요합니다."
            );
        }
        
        try {
            Long contractIdLong = Long.parseLong(request.getContractId());
            CertificateEntity result = mlAnalysisService.analyzeContract(contractIdLong, String.valueOf(userId));
            
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("analysisId", result.getId().toString());
            responseData.put("contractId", request.getContractId());
            responseData.put("analysisType", request.getAnalysisType());
            responseData.put("status", "PROCESSING");
            responseData.put("estimatedTime", "2-3분");
            responseData.put("startedAt", java.time.LocalDateTime.now().toString());
            
            log.info("✅ 계약서 분석 시작 성공 - Analysis ID: {}", result.getId());
            return ResponseEntity.ok(ApiResponse.success(responseData));
        } catch (NumberFormatException e) {
            log.error("❌ 잘못된 계약서 ID 형식: {}", request.getContractId());
            return ResponseEntity.badRequest().body(ApiResponse.error("VALIDATION_ERROR", "잘못된 계약서 ID 형식입니다: " + request.getContractId()));
        } catch (Exception e) {
            log.error("❌ 계약서 분석 시작 중 오류 발생", e);
            return ResponseEntity.internalServerError().body(ApiResponse.error("ANALYSIS_ERROR", "분석 시작 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }

    /**
     * READ - 분석 결과 개별 조회 (Analysis ID로)
     */
    @GetMapping("/{analysisId}")
    @Operation(summary = "분석 결과 조회", description = "분석 ID로 특정 분석 결과를 조회합니다.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "조회 성공",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "조회 성공 예시",
                    summary = "분석 결과 조회 성공",
                    value = """
                    {
                      \"isSuccess\": true,
                      \"code\": \"200\",
                      \"message\": \"요청에 성공했습니다.\",
                      \"result\": {
                        \"analysisId\": \"A-123\",
                        \"riskLevel\": \"MEDIUM\",
                        \"score\": 65
                      }
                    }
                    """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "분석 결과를 찾을 수 없음",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "분석 결과 없음 예시",
                    summary = "분석 결과를 찾을 수 없음",
                    value = """
                    {
                      \"isSuccess\": false,
                      \"code\": \"COMMON404\",
                      \"message\": \"분석 결과를 찾을 수 없습니다.\",
                      \"result\": null
                    }
                    """
                )
            )
        )
    })
    public ResponseEntity<ApiResponse<String>> getAnalysisById(
            @Parameter(description = "분석 ID", required = true) @PathVariable String analysisId) {
        log.info("🔍 분석 결과 조회 요청 - Analysis ID: {}", analysisId);
        try {
            // TODO: 실제 분석 결과 반환
            return ResponseEntity.ok(ApiResponse.success("구현 예정"));
        } catch (Exception e) {
            log.error("분석 결과 조회 실패", e);
            return ResponseEntity.internalServerError().body(ApiResponse.error("COMMON500", e.getMessage()));
        }
    }

    /**
     * READ - 사용자별 분석 결과 전체 조회
     */
    @GetMapping("/user/{userId}")
    @Operation(summary = "사용자별 분석 결과 조회", description = "특정 사용자의 모든 분석 결과를 조회합니다.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "조회 성공",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "사용자별 분석 결과 예시",
                    summary = "사용자별 분석 결과 조회 성공",
                    value = """
                    {
                      \"isSuccess\": true,
                      \"code\": \"200\",
                      \"message\": \"요청에 성공했습니다.\",
                      \"result\": [
                        { \"analysisId\": \"A-123\", \"riskLevel\": \"LOW\" },
                        { \"analysisId\": \"A-124\", \"riskLevel\": \"HIGH\" }
                      ]
                    }
                    """
                )
            )
        )
    })
    public ResponseEntity<ApiResponse<String>> getAnalysesByUserId(
            @Parameter(description = "사용자 ID", required = true) @PathVariable String userId) {
        log.info("👤 사용자별 분석 결과 조회 요청 - User ID: {}", userId);
        try {
            // TODO: 실제 분석 결과 반환
            return ResponseEntity.ok(ApiResponse.success("구현 예정"));
        } catch (Exception e) {
            log.error("사용자별 분석 결과 조회 실패", e);
            return ResponseEntity.internalServerError().body(ApiResponse.error("COMMON500", e.getMessage()));
        }
    }

    /**
     * DELETE - 분석 결과 삭제
     */
    @DeleteMapping("/{analysisId}")
    @Operation(summary = "분석 결과 삭제", description = "분석 ID로 특정 분석 결과를 삭제합니다.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "삭제 성공",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "삭제 성공 예시",
                    summary = "분석 결과 삭제 성공",
                    value = """
                    {
                      \"isSuccess\": true,
                      \"code\": \"200\",
                      \"message\": \"분석 결과가 성공적으로 삭제되었습니다.\",
                      \"result\": null
                    }
                    """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "분석 결과를 찾을 수 없음",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "분석 결과 없음 예시",
                    summary = "분석 결과를 찾을 수 없음",
                    value = """
                    {
                      \"isSuccess\": false,
                      \"code\": \"COMMON404\",
                      \"message\": \"분석 결과를 찾을 수 없습니다.\",
                      \"result\": null
                    }
                    """
                )
            )
        )
    })
    public ResponseEntity<ApiResponse<String>> deleteAnalysis(
            @Parameter(description = "분석 ID", required = true) @PathVariable String analysisId,
            @Parameter(description = "사용자 ID (보안 검증용)", required = true) @RequestParam String userId) {
        log.info("🗑️ 분석 결과 삭제 요청 - Analysis ID: {}, User ID: {}", analysisId, userId);
        try {
            // TODO: 실제 삭제 처리
            return ResponseEntity.ok(ApiResponse.success("구현 예정"));
        } catch (Exception e) {
            log.error("분석 결과 삭제 실패", e);
            return ResponseEntity.internalServerError().body(ApiResponse.error("COMMON500", e.getMessage()));
        }
    }

    @Operation(
        summary = "📊 계약서 분석 결과 조회", 
        description = """
        ## 📖 API 설명
        특정 계약서의 AI 분석 결과를 조회합니다. 분석된 계약서의 위험도, 주요 조항, 개선사항 등을 확인할 수 있습니다.
        
        ## 🎯 프론트엔드 구현 가이드
        
        ### 1. 분석 결과 조회 방법
        ```javascript
        const contractId = "123";
        const response = await fetch(`/api/analysis/contract/${contractId}`, {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });
        
        const result = await response.json();
        console.log('분석 결과:', result);
        ```
        
        ### 2. 응답 데이터 구조
        ```json
        {
            "analysis_id": 456,
            "contract_id": 123,
            "risk_level": "MEDIUM",
            "risk_score": 65,
            "analysis_date": "2025-01-15T10:30:00Z",
            "key_findings": [
                "보증금 반환 조건이 불명확합니다",
                "계약 기간이 명시되지 않았습니다"
            ],
            "recommendations": [
                "보증금 반환 조건을 구체적으로 명시하세요",
                "계약 기간을 명확히 작성하세요"
            ],
            "status": "COMPLETED"
        }
        ```
        
        ### 3. 위험도 레벨 (risk_level)
        - `LOW`: 낮은 위험 (0-30점)
        - `MEDIUM`: 중간 위험 (31-70점)
        - `HIGH`: 높은 위험 (71-100점)
        
        ### 4. 분석 상태 (status)
        - `PENDING`: 분석 대기 중
        - `PROCESSING`: 분석 진행 중
        - `COMPLETED`: 분석 완료
        - `FAILED`: 분석 실패
        """
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "✅ 분석 결과 조회 성공",
            content = @Content(
                examples = @ExampleObject(
                    value = """
                    {
                        "analysis_id": 456,
                        "contract_id": 123,
                        "risk_level": "MEDIUM",
                        "risk_score": 65,
                        "analysis_date": "2025-01-15T10:30:00Z",
                        "key_findings": [
                            "보증금 반환 조건이 불명확합니다",
                            "계약 기간이 명시되지 않았습니다"
                        ],
                        "recommendations": [
                            "보증금 반환 조건을 구체적으로 명시하세요",
                            "계약 기간을 명확히 작성하세요"
                        ],
                        "status": "COMPLETED"
                    }
                    """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404", 
            description = "❌ 분석 결과를 찾을 수 없음",
            content = @Content(
                examples = @ExampleObject(
                    value = """
                    {
                        "error": "ANALYSIS_NOT_FOUND",
                        "message": "해당 계약서의 분석 결과를 찾을 수 없습니다.",
                        "timestamp": "2025-01-15T10:30:00"
                    }
                    """
                )
            )
        )
    })
    @GetMapping("/contract/{contractId}")
    public ResponseEntity<MLAnalysisResult> getAnalysisByContractId(
            @Parameter(description = "계약서 ID", example = "123") @PathVariable Long contractId) {
        log.info("계약서 분석 결과 조회 요청 - 계약서 ID: {}", contractId);
        
        MLAnalysisResult result = mlAnalysisService.getAnalysisByContractId(contractId);
        if (result == null) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(result);
    }

    @Operation(
        summary = "📋 모든 분석 결과 조회", 
        description = """
        시스템에 저장된 모든 계약서 분석 결과를 조회합니다.
        
        **사용법:**
        ```javascript
        const response = await fetch('/api/analysis/all');
        const result = await response.json();
        console.log('전체 분석 결과:', result);
        ```
        
        **응답 예시:**
        ```json
        [
            {
                "analysis_id": 456,
                "contract_id": 123,
                "risk_level": "MEDIUM",
                "risk_score": 65,
                "analysis_date": "2025-01-15T10:30:00Z",
                "status": "COMPLETED"
            },
            {
                "analysis_id": 457,
                "contract_id": 124,
                "risk_level": "LOW",
                "risk_score": 25,
                "analysis_date": "2025-01-15T11:00:00Z",
                "status": "COMPLETED"
            }
        ]
        ```
        """
    )
    @GetMapping("/all")
    public ResponseEntity<List<MLAnalysisResult>> getAllAnalyses() {
        log.info("모든 분석 결과 조회 요청");
        
        List<MLAnalysisResult> results = mlAnalysisService.getAllAnalyses();
        return ResponseEntity.ok(results);
    }

    @Operation(
        summary = "🗑️ 분석 결과 삭제", 
        description = """
        특정 계약서의 분석 결과를 삭제합니다.
        
        **사용법:**
        ```javascript
        const contractId = "123";
        const response = await fetch(`/api/analysis/contract/${contractId}`, {
            method: 'DELETE',
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });
        
        if (response.ok) {
            console.log('분석 결과 삭제 완료');
        }
        ```
        
        **응답 예시:**
        ```json
        {
            "success": true,
            "message": "계약서 ID 123의 분석 결과가 삭제되었습니다."
        }
        ```
        
        **주의사항:**
        - 삭제된 분석 결과는 복구할 수 없습니다
        - 계약서 자체는 삭제되지 않습니다
        - 관리자 권한이 필요할 수 있습니다
        """
    )
    @DeleteMapping("/contract/{contractId}")
    public ResponseEntity<Map<String, Object>> deleteAnalysisByContractId(
            @Parameter(description = "계약서 ID", example = "123") @PathVariable Long contractId) {
        log.info("계약서 분석 결과 삭제 요청 - 계약서 ID: {}", contractId);
        
        mlAnalysisService.deleteAnalysisByContractId(contractId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "계약서 ID " + contractId + "의 분석 결과가 삭제되었습니다.");
        
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "📈 분석 통계 조회", 
        description = """
        계약서 분석 통계 정보를 조회합니다.
        
        **사용법:**
        ```javascript
        const response = await fetch('/api/analysis/stats');
        const result = await response.json();
        console.log('분석 통계:', result);
        ```
        
        **응답 예시:**
        ```json
        {
            "total_analyses": 150,
            "completed_analyses": 145,
            "pending_analyses": 3,
            "failed_analyses": 2,
            "average_risk_score": 45.2,
            "risk_distribution": {
                "LOW": 60,
                "MEDIUM": 70,
                "HIGH": 20
            },
            "last_updated": "2025-01-15T10:30:00Z"
        }
        ```
        """
    )
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getAnalysisStats() {
        log.info("분석 통계 조회 요청");
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("total_analyses", mlAnalysisService.getTotalAnalyses());
        stats.put("completed_analyses", mlAnalysisService.getCompletedAnalyses());
        stats.put("pending_analyses", mlAnalysisService.getPendingAnalyses());
        stats.put("failed_analyses", mlAnalysisService.getFailedAnalyses());
        stats.put("average_risk_score", mlAnalysisService.getAverageRiskScore());
        stats.put("risk_distribution", mlAnalysisService.getRiskDistribution());
        stats.put("last_updated", LocalDateTime.now().atOffset(ZoneOffset.UTC).toString());
        
        return ResponseEntity.ok(stats);
    }
}