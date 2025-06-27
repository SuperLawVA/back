package com.superlawva.domain.ml.controller;

import com.superlawva.domain.ml.service.MLAnalysisService;
import com.superlawva.domain.ml.entity.MLAnalysisResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Slf4j
@RestController
@RequestMapping("/api/analysis")
@RequiredArgsConstructor
@Tag(name = "Contract Analysis", description = "계약서 분석 CRD API")
public class MLAnalysisController {

    private final MLAnalysisService mlAnalysisService;

    /**
     * CREATE - 계약서 분석 생성
     */
    @PostMapping("/contract/{contractId}")
    @Operation(summary = "계약서 분석 생성", description = "계약서 ID로 계약서를 조회하여 ML 분석을 수행하고 결과를 저장합니다.")
    @ApiResponse(responseCode = "200", description = "분석 성공")
    @ApiResponse(responseCode = "400", description = "잘못된 요청")
    @ApiResponse(responseCode = "404", description = "계약서를 찾을 수 없음")
    @ApiResponse(responseCode = "500", description = "서버 오류")
    public ResponseEntity<Map<String, Object>> createAnalysis(
            @Parameter(description = "계약서 ID", required = true) @PathVariable String contractId) {

        log.info("📊 계약서 분석 생성 요청 - Contract ID: {}", contractId);

        try {
            // 분석 서비스 호출 (userId 필요 없음)
            Map<String, Object> result = mlAnalysisService.analyzeContract(contractId);

            if (result.get("success").equals(true)) {
                log.info("✅ 계약서 분석 생성 성공");
                return ResponseEntity.ok(result);
            } else {
                log.error("❌ 계약서 분석 생성 실패");
                return ResponseEntity.badRequest().body(result);
            }
        } catch (Exception e) {
            log.error("계약서 분석 생성 중 오류 발생", e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "error", e.getMessage(),
                    "contractId", contractId,
                    "timestamp", LocalDateTime.now(ZoneOffset.UTC)
            ));
        }
    }

    /**
     * READ - 분석 결과 개별 조회 (Analysis ID로)
     */
    @GetMapping("/{analysisId}")
    @Operation(summary = "분석 결과 조회", description = "분석 ID로 특정 분석 결과를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @ApiResponse(responseCode = "404", description = "분석 결과를 찾을 수 없음")
    public ResponseEntity<Map<String, Object>> getAnalysisById(
            @Parameter(description = "분석 ID", required = true) @PathVariable String analysisId) {

        log.info("🔍 분석 결과 조회 요청 - Analysis ID: {}", analysisId);

        try {
            MLAnalysisResult analysis = mlAnalysisService.getAnalysisById(analysisId);

            if (analysis != null) {
                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "analysis", analysis,
                        "timestamp", LocalDateTime.now(ZoneOffset.UTC)
                ));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("분석 결과 조회 실패", e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "error", e.getMessage(),
                    "analysisId", analysisId,
                    "timestamp", LocalDateTime.now(ZoneOffset.UTC)
            ));
        }
    }

    /**
     * READ - 사용자별 분석 결과 전체 조회
     */
    @GetMapping("/user/{userId}")
    @Operation(summary = "사용자별 분석 결과 조회", description = "특정 사용자의 모든 분석 결과를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    public ResponseEntity<Map<String, Object>> getAnalysesByUserId(
            @Parameter(description = "사용자 ID", required = true) @PathVariable String userId) {

        log.info("👤 사용자별 분석 결과 조회 요청 - User ID: {}", userId);

        try {
            List<MLAnalysisResult> analyses = mlAnalysisService.getAnalysesByUserId(userId);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "userId", userId,
                    "count", analyses.size(),
                    "analyses", analyses,
                    "timestamp", LocalDateTime.now(ZoneOffset.UTC)
            ));
        } catch (Exception e) {
            log.error("사용자별 분석 결과 조회 실패", e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "error", e.getMessage(),
                    "userId", userId,
                    "timestamp", LocalDateTime.now(ZoneOffset.UTC)
            ));
        }
    }



    /**
     * DELETE - 분석 결과 삭제
     */
    @DeleteMapping("/{analysisId}")
    @Operation(summary = "분석 결과 삭제", description = "분석 ID로 특정 분석 결과를 삭제합니다.")
    @ApiResponse(responseCode = "200", description = "삭제 성공")
    @ApiResponse(responseCode = "404", description = "분석 결과를 찾을 수 없음")
    public ResponseEntity<Map<String, Object>> deleteAnalysis(
            @Parameter(description = "분석 ID", required = true) @PathVariable String analysisId,
            @Parameter(description = "사용자 ID (보안 검증용)", required = true) @RequestParam String userId) {

        log.info("🗑️ 분석 결과 삭제 요청 - Analysis ID: {}, User ID: {}", analysisId, userId);

        try {
            boolean deleted = mlAnalysisService.deleteAnalysis(analysisId, userId);

            if (deleted) {
                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "message", "분석 결과가 성공적으로 삭제되었습니다.",
                        "analysisId", analysisId,
                        "timestamp", LocalDateTime.now(ZoneOffset.UTC)
                ));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("분석 결과 삭제 실패", e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "error", e.getMessage(),
                    "analysisId", analysisId,
                    "timestamp", LocalDateTime.now(ZoneOffset.UTC)
            ));
        }
    }


}