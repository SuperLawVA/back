package com.superlawva.domain.ml.controller;

import com.superlawva.domain.ml.service.MLAnalysisService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/ml")
@RequiredArgsConstructor
@Tag(name = "ML Analysis", description = "ML 기반 계약서 분석 및 문서 생성 API")
public class MLAnalysisController {

    private final MLAnalysisService mlAnalysisService;

    @PostMapping("/analyze/contract/{contractId}")
    @Operation(summary = "계약서 분석", description = "ML API를 통해 계약서를 분석하고 위험도를 평가합니다.")
    @ApiResponse(responseCode = "200", description = "분석 성공")
    @ApiResponse(responseCode = "400", description = "잘못된 요청")
    @ApiResponse(responseCode = "500", description = "서버 오류")
    public ResponseEntity<Map<String, Object>> analyzeContract(
            @Parameter(description = "계약서 ID", required = true) @PathVariable String contractId,
            @Parameter(description = "사용자 ID", required = true) @RequestParam String userId) {
        
        log.info("📊 계약서 분석 요청 - Contract ID: {}, User ID: {}", contractId, userId);
        
        try {
            Map<String, Object> result = mlAnalysisService.analyzeContract(contractId, userId);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("계약서 분석 실패", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "error", e.getMessage(),
                "contractId", contractId,
                "userId", userId
            ));
        }
    }

    @PostMapping("/generate/proof/{contractId}")
    @Operation(summary = "내용증명서 생성", description = "계약서 기반으로 내용증명서를 생성합니다.")
    public ResponseEntity<Map<String, Object>> generateProofDocument(
            @Parameter(description = "계약서 ID", required = true) @PathVariable String contractId,
            @Parameter(description = "사용자 ID", required = true) @RequestParam String userId) {
        
        log.info("📝 내용증명서 생성 요청 - Contract ID: {}, User ID: {}", contractId, userId);
        
        try {
            Map<String, Object> result = mlAnalysisService.generateProofDocument(contractId, userId);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("내용증명서 생성 실패", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "error", e.getMessage(),
                "contractId", contractId,
                "userId", userId
            ));
        }
    }

    @PostMapping("/generate/special-terms/{contractId}")
    @Operation(summary = "특약사항 생성", description = "계약서 기반으로 특약사항을 생성합니다.")
    public ResponseEntity<Map<String, Object>> generateSpecialTerms(
            @Parameter(description = "계약서 ID", required = true) @PathVariable String contractId,
            @Parameter(description = "사용자 ID", required = true) @RequestParam String userId) {
        
        log.info("⚖️ 특약사항 생성 요청 - Contract ID: {}, User ID: {}", contractId, userId);
        
        try {
            Map<String, Object> result = mlAnalysisService.generateSpecialTerms(contractId, userId);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("특약사항 생성 실패", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "error", e.getMessage(),
                "contractId", contractId,
                "userId", userId
            ));
        }
    }

    @GetMapping("/test/connection")
    @Operation(summary = "ML API 연결 테스트", description = "ML API 서버 연결 상태를 확인합니다.")
    public ResponseEntity<Map<String, Object>> testMLConnection() {
        log.info("🔍 ML API 연결 테스트 시작");
        
        try {
            Map<String, Object> testResult = mlAnalysisService.testMLApiConnection();
            log.info("✅ ML API 연결 테스트 완료");
            return ResponseEntity.ok(testResult);
        } catch (Exception e) {
            log.error("❌ ML API 연결 테스트 실패", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "error", e.getMessage(),
                "timestamp", System.currentTimeMillis()
            ));
        }
    }

    @PostMapping("/test/analyze/{contractId}")
    @Operation(summary = "계약서 분석 테스트 (상세 로깅)", description = "계약서 분석을 상세 로깅과 함께 실행합니다.")
    public ResponseEntity<Map<String, Object>> testAnalyzeContract(
            @Parameter(description = "계약서 ID", required = true) @PathVariable String contractId,
            @Parameter(description = "사용자 ID", required = true) @RequestParam String userId) {
        
        log.info("🧪 계약서 분석 테스트 시작 - Contract ID: {}, User ID: {}", contractId, userId);
        
        try {
            Map<String, Object> result = mlAnalysisService.testContractAnalysis(contractId, userId);
            log.info("✅ 계약서 분석 테스트 완료");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("❌ 계약서 분석 테스트 실패", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "error", e.getMessage(),
                "contractId", contractId,
                "userId", userId,
                "timestamp", System.currentTimeMillis()
            ));
        }
    }
}