package com.superlawva.domain.ocr3.controller;

import com.superlawva.domain.ocr3.dto.ErrorResponse;
import com.superlawva.domain.ocr3.dto.OcrResponse;
import com.superlawva.domain.ocr3.service.OcrService;
import com.superlawva.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.superlawva.global.security.annotation.LoginUser;

@Slf4j
@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "*") // Configure based on your requirements
@Tag(name = "📄 OCR & Contract Analysis", description = "계약서 OCR 처리 및 분석 API")
public class OcrController {

    private final OcrService ocrService;

    @Operation(
        summary = "📄 계약서 OCR 처리 (사용자별)", 
        description = """
        ## 📖 API 설명
        계약서 이미지를 업로드하여 OCR 처리 후 AI 분석을 통해 구조화된 계약서 데이터를 생성합니다.
   
        ### 2. 지원 파일 형식
        - **이미지**: JPG, PNG, JPEG
        - **문서**: PDF (최대 10MB)
        - **해상도**: 최소 300DPI 권장
        
        ### 3. 처리 과정
        1. **파일 업로드** → S3 저장
        2. **OCR 처리** → 텍스트 추출
        3. **AI 분석** → Gemini API로 구조화
        4. **데이터 저장** → MySQL DB 저장
        5. **결과 반환** → 구조화된 계약서 데이터
        
        
        **주의사항:**
        - 파일 크기는 10MB 이하여야 합니다
        - 이미지 품질이 좋을수록 OCR 정확도가 높아집니다
        - 처리 시간은 파일 크기에 따라 10-30초 소요됩니다
        """
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "✅ OCR 처리 성공",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "OCR 성공 예시",
                    summary = "OCR 처리 성공",
                    value = """
                    {
                      "isSuccess": true,
                      "code": "200",
                      "message": "요청에 성공했습니다.",
                      "result": {
                        "contractData": {
                          "id": 123,
                          "userId": "user123",
                          "contractType": "전세",
                          "dates": { "contractDate": "2025-01-15" },
                          "property": { "address": "서울시 강남구..." },
                          "payment": { "deposit": 80000000 },
                          "lessor": { "name": "이영희" },
                          "lessee": { "name": "김민준" }
                        },
                        "debugMode": false
                      }
                    }
                    """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "❌ 잘못된 요청 (파일 없음 또는 지원하지 않는 형식)",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "잘못된 요청 예시",
                    summary = "파일 없음",
                    value = """
                    {
                      "isSuccess": false,
                      "code": "OCR400",
                      "message": "파일이 비어있습니다.",
                      "result": null
                    }
                    """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "500",
            description = "❌ 서버 오류 (OCR 처리 실패)",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "서버 오류 예시",
                    summary = "OCR 처리 중 서버 오류",
                    value = """
                    {
                      "isSuccess": false,
                      "code": "OCR500",
                      "message": "사용자 OCR 처리 중 오류가 발생했습니다: 파일 형식을 확인해주세요.",
                      "result": null
                    }
                    """
                )
            )
        )
    })
    @PostMapping(value = "/upload/ocr3", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<OcrResponse>> uploadAndProcessContractAuth(
            @Parameter(hidden = true) @LoginUser Long userId,
            @Parameter(description = "계약서 이미지 파일 (JPG, PNG, PDF)", required = true) @RequestParam("file") @NotNull MultipartFile file) {

        // 인증되지 않은 경우 게스트 처리 가능
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("UNAUTHORIZED", "인증 정보가 없습니다."));
        }

        log.info("Received OCR request (auth) for userId: {}", userId);
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("OCR400", "파일이 비어있습니다."));
            }

            OcrResponse response = ocrService.processContractWithUserId(file, String.valueOf(userId));
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (Exception e) {
            log.error("Error processing OCR (auth) for userId: {}", userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("OCR500", "OCR 처리 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }

    @Operation(
        summary = "📋 모든 계약서 조회", 
        description = "시스템에 저장된 모든 계약서 목록을 조회합니다."
    )
    @GetMapping("/upload/ocr3/contracts/all")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAllContracts() {
        log.info("Retrieving all contracts");

        try {
            List<com.superlawva.domain.ocr3.entity.ContractData> contracts = ocrService.getAllContracts();

            Map<String, Object> responseData = new HashMap<>();
            responseData.put("count", contracts.size());
            responseData.put("contracts", contracts);

            return ResponseEntity.ok(ApiResponse.success(responseData));

        } catch (Exception e) {
            log.error("Error retrieving contracts: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("RETRIEVAL_ERROR", "계약서 목록 조회 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }

    @Operation(
        summary = "🔍 특정 계약서 조회", 
        description = "계약서 ID로 특정 계약서의 상세 정보를 조회합니다."
    )
    @GetMapping("/upload/ocr3/contracts/{id}")
    public ResponseEntity<ApiResponse<com.superlawva.domain.ocr3.entity.ContractData>> getContractById(
            @Parameter(description = "계약서 ID", example = "123") @PathVariable String id) {
        log.info("Retrieving contract with ID: {}", id);

        try {
            // String을 Long으로 변환
            Long contractId = Long.parseLong(id);
            com.superlawva.domain.ocr3.entity.ContractData contract = ocrService.getContractById(contractId);

            if (contract != null) {
                log.info("Found contract: {} with type: {}", contract.getId(), contract.getContractType());
                return ResponseEntity.ok(ApiResponse.success(contract));
            } else {
                log.warn("Contract not found with ID: {}", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("CONTRACT_NOT_FOUND", "계약서를 찾을 수 없습니다: " + id));
            }
        } catch (NumberFormatException e) {
            log.error("Invalid contract ID format: {}", id);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("INVALID_ID_FORMAT", "잘못된 계약서 ID 형식입니다: " + id));
        } catch (Exception e) {
            log.error("Error retrieving contract by ID", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("RETRIEVAL_ERROR", "계약서 조회 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }

    @Operation(
        summary = "👤 사용자별 계약서 조회", 
        description = "특정 사용자가 업로드한 모든 계약서 목록을 조회합니다."
    )
    @GetMapping("/upload/ocr3/user/{userId}/contracts")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getContractsByUserId(
            @Parameter(description = "사용자 ID", example = "user123") @PathVariable String userId) {
        log.info("Retrieving contracts for userId: {}", userId);

        try {
            List<com.superlawva.domain.ocr3.entity.ContractData> contracts = ocrService.getContractsByUserId(userId);

            log.info("Found {} contracts for userId: {}", contracts.size(), userId);

            Map<String, Object> responseData = new HashMap<>();
            responseData.put("userId", userId);
            responseData.put("count", contracts.size());
            responseData.put("contracts", contracts);

            return ResponseEntity.ok(ApiResponse.success(responseData));

        } catch (Exception e) {
            log.error("Error retrieving contracts for userId: {}", userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("USER_CONTRACTS_ERROR", "사용자 계약서 조회 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }
}