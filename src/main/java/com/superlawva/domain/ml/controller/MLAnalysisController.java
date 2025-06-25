package com.superlawva.domain.ml.controller;

import com.superlawva.domain.document.entity.GeneratedDocument;
import com.superlawva.domain.ml.service.MLAnalysisService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/ml")
@Tag(name = "🤖 ML Analysis API", description = "AI 기반 계약서 분석 및 내용증명서 생성 API")
public class MLAnalysisController {

    private final MLAnalysisService mlAnalysisService;

    @PostMapping("/analyze/contract/{contractId}")
    @Operation(
        summary = "AI 계약서 분석",
        description = "MongoDB의 contract 컬렉션 데이터를 ML 서버로 전송하여 AI 분석 후 generated_contract에 저장"
    )
    @ApiResponse(responseCode = "200", description = "계약서 분석 성공")
    @ApiResponse(responseCode = "404", description = "계약서를 찾을 수 없음")
    @ApiResponse(responseCode = "500", description = "ML 서버 오류")
    public ResponseEntity<Map<String, Object>> analyzeContract(
            @Parameter(description = "분석할 계약서 ID", required = true)
            @PathVariable String contractId,
            @Parameter(description = "사용자 ID", required = true)
            @RequestParam String userId
    ) {
        log.info("🤖 계약서 분석 API 호출 - Contract ID: {}, User ID: {}", contractId, userId);

        try {
            long startTime = System.currentTimeMillis();

            // ML 분석 수행
            GeneratedDocument result = mlAnalysisService.analyzeContract(contractId, userId);

            double processingTime = (System.currentTimeMillis() - startTime) / 1000.0;

            // 응답 데이터 구성
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "계약서 분석이 완료되었습니다.");
            response.put("data", Map.of(
                "generatedDocumentId", result.getId(),
                "documentId", result.getDocumentId(),
                "userId", result.getUserId(),
                "generationType", result.getGenerationType(),
                "status", result.getStatus(),
                "content", result.getAdditionalMetadata() != null ? result.getAdditionalMetadata().get("content") : "내용 없음",
                "processingTime", processingTime,
                "createdAt", result.getCreatedAt(),
                "updatedAt", result.getUpdatedAt()
            ));

            log.info("✅ 계약서 분석 API 완료 - Generated ID: {}, 처리시간: {}초",
                     result.getId(), processingTime);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("❌ 계약서 분석 API 실패 - Contract ID: {}", contractId, e);

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "계약서 분석 중 오류가 발생했습니다: " + e.getMessage());
            errorResponse.put("error", Map.of(
                "contractId", contractId,
                "userId", userId,
                "errorType", e.getClass().getSimpleName(),
                "timestamp", System.currentTimeMillis()
            ));

            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    @PostMapping("/generate/proof/{contractId}")
    @Operation(
        summary = "AI 내용증명서 생성",
        description = "MongoDB의 contract 컬렉션 데이터를 기반으로 AI 내용증명서 생성 후 generated_contract에 저장"
    )
    @ApiResponse(responseCode = "200", description = "내용증명서 생성 성공")
    @ApiResponse(responseCode = "404", description = "계약서를 찾을 수 없음")
    @ApiResponse(responseCode = "500", description = "ML 서버 오류")
    public ResponseEntity<Map<String, Object>> generateProofDocument(
            @Parameter(description = "내용증명서 생성할 계약서 ID", required = true)
            @PathVariable String contractId,
            @Parameter(description = "사용자 ID", required = true)
            @RequestParam String userId
    ) {
        log.info("📝 내용증명서 생성 API 호출 - Contract ID: {}, User ID: {}", contractId, userId);

        try {
            long startTime = System.currentTimeMillis();

            // ML 내용증명서 생성 수행
            GeneratedDocument result = mlAnalysisService.generateProofDocument(contractId, userId);

            double processingTime = (System.currentTimeMillis() - startTime) / 1000.0;

            // 응답 데이터 구성
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "내용증명서 생성이 완료되었습니다.");
            response.put("data", Map.of(
                "generatedDocumentId", result.getId(),
                "documentId", result.getDocumentId(),
                "userId", result.getUserId(),
                "generationType", result.getGenerationType(),
                "status", result.getStatus(),
                "content", result.getAdditionalMetadata() != null ? result.getAdditionalMetadata().get("content") : "내용 없음",
                "processingTime", processingTime,
                "createdAt", result.getCreatedAt(),
                "updatedAt", result.getUpdatedAt()
            ));

            log.info("✅ 내용증명서 생성 API 완료 - Generated ID: {}, 처리시간: {}초",
                     result.getId(), processingTime);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("❌ 내용증명서 생성 API 실패 - Contract ID: {}", contractId, e);

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "내용증명서 생성 중 오류가 발생했습니다: " + e.getMessage());
            errorResponse.put("error", Map.of(
                "contractId", contractId,
                "userId", userId,
                "errorType", e.getClass().getSimpleName(),
                "timestamp", System.currentTimeMillis()
            ));

            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    @PostMapping("/generate/special-terms/{contractId}")
    @Operation(
        summary = "AI 특약사항 생성",
        description = "MongoDB의 contract 컬렉션 데이터를 기반으로 AI 특약사항 생성 후 generated_contract에 저장"
    )
    @ApiResponse(responseCode = "200", description = "특약사항 생성 성공")
    @ApiResponse(responseCode = "404", description = "계약서를 찾을 수 없음")
    @ApiResponse(responseCode = "500", description = "ML 서버 오류")
    public ResponseEntity<Map<String, Object>> generateSpecialTerms(
            @Parameter(description = "특약사항 생성할 계약서 ID", required = true)
            @PathVariable String contractId,
            @Parameter(description = "사용자 ID", required = true)
            @RequestParam String userId
    ) {
        log.info("⚖️ 특약사항 생성 API 호출 - Contract ID: {}, User ID: {}", contractId, userId);

        try {
            long startTime = System.currentTimeMillis();

            // ML 특약사항 생성 수행
            GeneratedDocument result = mlAnalysisService.generateSpecialTerms(contractId, userId);

            double processingTime = (System.currentTimeMillis() - startTime) / 1000.0;

            // 응답 데이터 구성
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "특약사항 생성이 완료되었습니다.");
            response.put("data", Map.of(
                "generatedDocumentId", result.getId(),
                "documentId", result.getDocumentId(),
                "userId", result.getUserId(),
                "generationType", result.getGenerationType(),
                "status", result.getStatus(),
                "content", result.getAdditionalMetadata() != null ? result.getAdditionalMetadata().get("content") : "내용 없음",
                "processingTime", processingTime,
                "createdAt", result.getCreatedAt(),
                "updatedAt", result.getUpdatedAt()
            ));

            log.info("✅ 특약사항 생성 API 완료 - Generated ID: {}, 처리시간: {}초",
                     result.getId(), processingTime);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("❌ 특약사항 생성 API 실패 - Contract ID: {}", contractId, e);

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "특약사항 생성 중 오류가 발생했습니다: " + e.getMessage());
            errorResponse.put("error", Map.of(
                "contractId", contractId,
                "userId", userId,
                "errorType", e.getClass().getSimpleName(),
                "timestamp", System.currentTimeMillis()
            ));

            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    @GetMapping("/results/{generatedDocumentId}")
    @Operation(
        summary = "생성된 문서 결과 조회",
        description = "generated_contract 컬렉션에서 생성된 문서 결과를 조회"
    )
    @ApiResponse(responseCode = "200", description = "결과 조회 성공")
    @ApiResponse(responseCode = "404", description = "생성된 문서를 찾을 수 없음")
    public ResponseEntity<Map<String, Object>> getGeneratedResult(
            @Parameter(description = "생성된 문서 ID", required = true)
            @PathVariable String generatedDocumentId
    ) {
        log.info("📄 생성된 문서 결과 조회 - Generated Document ID: {}", generatedDocumentId);

        try {
            GeneratedDocument result = mlAnalysisService.getGeneratedResult(generatedDocumentId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "생성된 문서 결과를 성공적으로 조회했습니다.");
            response.put("data", Map.of(
                "generatedDocumentId", result.getId(),
                "documentId", result.getDocumentId(),
                "userId", result.getUserId(),
                "generationType", result.getGenerationType(),
                "status", result.getStatus(),
                "content", result.getAdditionalMetadata() != null ? result.getAdditionalMetadata().get("content") : "내용 없음",
                "createdAt", result.getCreatedAt(),
                "updatedAt", result.getUpdatedAt()
            ));

            log.info("✅ 생성된 문서 결과 조회 완료 - Generated Document ID: {}", generatedDocumentId);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("❌ 생성된 문서 결과 조회 실패 - Generated Document ID: {}", generatedDocumentId, e);

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "생성된 문서 결과 조회 중 오류가 발생했습니다: " + e.getMessage());
            errorResponse.put("error", Map.of(
                "generatedDocumentId", generatedDocumentId,
                "errorType", e.getClass().getSimpleName(),
                "timestamp", System.currentTimeMillis()
            ));

            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    @GetMapping("/health")
    @Operation(
        summary = "ML 서비스 헬스체크",
        description = "ML 서버와의 연결 상태 및 서비스 상태를 확인"
    )
    @ApiResponse(responseCode = "200", description = "ML 서비스 정상")
    @ApiResponse(responseCode = "503", description = "ML 서비스 오류")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        log.info("🏥 ML 서비스 헬스체크 요청");

        try {
            boolean isHealthy = mlAnalysisService.healthCheck();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "ML 서비스가 정상적으로 작동하고 있습니다.");
            response.put("data", Map.of(
                "service", "ML Analysis Service",
                "status", isHealthy ? "HEALTHY" : "UNHEALTHY",
                "timestamp", System.currentTimeMillis()
            ));

            log.info("✅ ML 서비스 헬스체크 완료 - 상태: {}", isHealthy ? "정상" : "오류");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("❌ ML 서비스 헬스체크 실패", e);

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "ML 서비스 헬스체크 중 오류가 발생했습니다: " + e.getMessage());
            errorResponse.put("error", Map.of(
                "service", "ML Analysis Service",
                "status", "UNHEALTHY",
                "errorType", e.getClass().getSimpleName(),
                "timestamp", System.currentTimeMillis()
            ));

            return ResponseEntity.status(503).body(errorResponse);
        }
    }
}