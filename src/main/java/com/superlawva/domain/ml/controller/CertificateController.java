package com.superlawva.domain.ml.controller;

import com.superlawva.domain.ml.dto.CertificateCreateRequest;
import com.superlawva.domain.ml.dto.CertificateResponse;
import com.superlawva.domain.ml.dto.CertificateUpdateRequest;
import com.superlawva.domain.ml.service.CertificateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Slf4j
@RestController
@RequestMapping("/api/certificate")
@RequiredArgsConstructor
@Tag(name = "Certificate", description = "내용증명서 CRUD API")
public class CertificateController {

    private final CertificateService certificateService;

    /**
     * CREATE - 내용증명서 생성
     */
    @PostMapping("/create")
    @Operation(summary = "내용증명서 생성", description = "계약서 ID와 사용자 요청사항을 받아 내용증명서를 생성합니다.")
    @ApiResponse(responseCode = "201", description = "생성 성공")
    @ApiResponse(responseCode = "400", description = "잘못된 요청")
    @ApiResponse(responseCode = "404", description = "계약서를 찾을 수 없음")
    @ApiResponse(responseCode = "500", description = "서버 오류")
    public ResponseEntity<Map<String, Object>> createCertificate(
            @Parameter(description = "계약서 ID", required = true) @RequestParam String contractId,
            @Parameter(description = "사용자 요청사항", required = true) @RequestParam String userQuery) {

        log.info("📝 내용증명서 생성 요청 - Contract ID: {}", contractId);
        log.info("   요청 내용: {}", userQuery);

        try {
            // CertificateCreateRequest 객체 생성
            CertificateCreateRequest request = new CertificateCreateRequest();
            request.setContractId(contractId);
            request.setUserQuery(userQuery);
            request.setDebugMode(false); // 기본값 설정
            
            CertificateResponse certificate = certificateService.createCertificate(request);

            Map<String, Object> response = new HashMap<>();
            response.put("success", certificate.getStatus().equals("SUCCESS"));
            response.put("certificate", certificate);
            response.put("timestamp", LocalDateTime.now(ZoneOffset.UTC));

            if (certificate.getStatus().equals("SUCCESS")) {
                log.info("✅ 내용증명서 생성 성공 - Certificate ID: {}", certificate.getId());
                return ResponseEntity.status(HttpStatus.CREATED).body(response);
            } else {
                log.error("❌ 내용증명서 생성 실패 - Error: {}", certificate.getErrorMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }
        } catch (Exception e) {
            log.error("내용증명서 생성 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "error", e.getMessage(),
                    "contractId", contractId,
                    "userQuery", userQuery,
                    "timestamp", LocalDateTime.now(ZoneOffset.UTC)
            ));
        }
    }

    /**
     * READ - 내용증명서 개별 조회 (Certificate ID로)
     */
    @GetMapping("/{certificateId}")
    @Operation(summary = "내용증명서 조회", description = "내용증명서 ID로 특정 내용증명서를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @ApiResponse(responseCode = "404", description = "내용증명서를 찾을 수 없음")
    public ResponseEntity<Map<String, Object>> getCertificateById(
            @Parameter(description = "내용증명서 ID", required = true) @PathVariable String certificateId) {

        log.info("🔍 내용증명서 조회 요청 - Certificate ID: {}", certificateId);

        try {
            CertificateResponse certificate = certificateService.getCertificateById(certificateId);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "certificate", certificate,
                    "timestamp", LocalDateTime.now(ZoneOffset.UTC)
            ));

        } catch (RuntimeException e) {
            if (e.getMessage().contains("찾을 수 없습니다")) {
                return ResponseEntity.notFound().build();
            }
            throw e;
        } catch (Exception e) {
            log.error("내용증명서 조회 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "error", e.getMessage(),
                    "certificateId", certificateId,
                    "timestamp", LocalDateTime.now(ZoneOffset.UTC)
            ));
        }
    }

    /**
     * READ - 사용자별 내용증명서 전체 조회
     */
    @GetMapping("/user/{userId}")
    @Operation(summary = "사용자별 내용증명서 조회", description = "특정 사용자의 모든 내용증명서를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    public ResponseEntity<Map<String, Object>> getCertificatesByUserId(
            @Parameter(description = "사용자 ID", required = true) @PathVariable String userId) {

        log.info("👤 사용자별 내용증명서 조회 요청 - User ID: {}", userId);

        try {
            List<CertificateResponse> certificates = certificateService.getCertificatesByUserId(userId);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "userId", userId,
                    "count", certificates.size(),
                    "certificates", certificates,
                    "timestamp", LocalDateTime.now(ZoneOffset.UTC)
            ));
        } catch (Exception e) {
            log.error("사용자별 내용증명서 조회 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "error", e.getMessage(),
                    "userId", userId,
                    "timestamp", LocalDateTime.now(ZoneOffset.UTC)
            ));
        }
    }



    /**
     * UPDATE - 내용증명서 수정
     */
    @PutMapping("/{certificateId}")
    @Operation(summary = "내용증명서 수정", description = "내용증명서 ID로 특정 내용증명서를 수정합니다.")
    @ApiResponse(responseCode = "200", description = "수정 성공")
    @ApiResponse(responseCode = "403", description = "권한 없음")
    @ApiResponse(responseCode = "404", description = "내용증명서를 찾을 수 없음")
    public ResponseEntity<Map<String, Object>> updateCertificate(
            @Parameter(description = "내용증명서 ID", required = true) @PathVariable String certificateId,
            @Parameter(description = "수정 요청", required = true) @Valid @RequestBody CertificateUpdateRequest request,
            @Parameter(description = "사용자 ID (보안 검증용)", required = true) @RequestParam String userId) {

        log.info("✏️ 내용증명서 수정 요청 - Certificate ID: {}, User ID: {}", certificateId, userId);

        try {
            CertificateResponse updatedCertificate = certificateService.updateCertificate(certificateId, request, userId);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "내용증명서가 성공적으로 수정되었습니다.",
                    "certificate", updatedCertificate,
                    "timestamp", LocalDateTime.now(ZoneOffset.UTC)
            ));

        } catch (RuntimeException e) {
            if (e.getMessage().contains("권한이 없습니다")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                        "success", false,
                        "error", e.getMessage(),
                        "certificateId", certificateId,
                        "timestamp", LocalDateTime.now(ZoneOffset.UTC)
                ));
            } else if (e.getMessage().contains("찾을 수 없습니다")) {
                return ResponseEntity.notFound().build();
            }
            throw e;
        } catch (Exception e) {
            log.error("내용증명서 수정 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "error", e.getMessage(),
                    "certificateId", certificateId,
                    "timestamp", LocalDateTime.now(ZoneOffset.UTC)
            ));
        }
    }

    /**
     * DELETE - 내용증명서 삭제
     */
    @DeleteMapping("/{certificateId}")
    @Operation(summary = "내용증명서 삭제", description = "내용증명서 ID로 특정 내용증명서를 삭제합니다.")
    @ApiResponse(responseCode = "200", description = "삭제 성공")
    @ApiResponse(responseCode = "403", description = "권한 없음")
    @ApiResponse(responseCode = "404", description = "내용증명서를 찾을 수 없음")
    public ResponseEntity<Map<String, Object>> deleteCertificate(
            @Parameter(description = "내용증명서 ID", required = true) @PathVariable String certificateId,
            @Parameter(description = "사용자 ID (보안 검증용)", required = true) @RequestParam String userId) {

        log.info("🗑️ 내용증명서 삭제 요청 - Certificate ID: {}, User ID: {}", certificateId, userId);

        try {
            boolean deleted = certificateService.deleteCertificate(certificateId, userId);

            if (deleted) {
                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "message", "내용증명서가 성공적으로 삭제되었습니다.",
                        "certificateId", certificateId,
                        "timestamp", LocalDateTime.now(ZoneOffset.UTC)
                ));
            } else {
                return ResponseEntity.notFound().build();
            }

        } catch (RuntimeException e) {
            if (e.getMessage().contains("권한이 없습니다")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                        "success", false,
                        "error", e.getMessage(),
                        "certificateId", certificateId,
                        "timestamp", LocalDateTime.now(ZoneOffset.UTC)
                ));
            } else if (e.getMessage().contains("찾을 수 없습니다")) {
                return ResponseEntity.notFound().build();
            }
            throw e;
        } catch (Exception e) {
            log.error("내용증명서 삭제 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "error", e.getMessage(),
                    "certificateId", certificateId,
                    "timestamp", LocalDateTime.now(ZoneOffset.UTC)
            ));
        }
    }


} 