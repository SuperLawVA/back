package com.superlawva.domain.ocr3.controller;

import com.superlawva.domain.ocr3.dto.ErrorResponse;
import com.superlawva.domain.ocr3.dto.OcrResponse;
import com.superlawva.domain.ocr3.service.OcrService;
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

@Slf4j
@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "*") // Configure based on your requirements
public class OcrController {

    private final OcrService ocrService;





    // 모든 계약서 조회
    @GetMapping("/api/upload/ocr3/contracts/all")
    public ResponseEntity<?> getAllContracts() {
        log.info("Retrieving all contracts");

        try {
            java.util.List<com.superlawva.domain.ocr3.entity.ContractData> contracts =
                    ocrService.getAllContracts();

            return ResponseEntity.ok(java.util.Map.of(
                    "success", true,
                    "count", contracts.size(),
                    "contracts", contracts
            ));
        } catch (Exception e) {
            log.error("Error retrieving contracts: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ErrorResponse.builder()
                            .error("RETRIEVAL_ERROR")
                            .message("계약서 목록 조회 중 오류가 발생했습니다: " + e.getMessage())
                            .timestamp(LocalDateTime.now().toString())
                            .build());
        }
    }



    // 🟢 MongoDB Atlas에서 특정 ID로 계약서 조회
    @GetMapping("/api/upload/ocr3/contracts/{id}")
    public ResponseEntity<?> getContractById(@PathVariable String id) {
        log.info("Retrieving contract with ID: {} from MongoDB Atlas", id);

        try {
            com.superlawva.domain.ocr3.entity.ContractData contract =
                    ocrService.getContractById(id);

            if (contract != null) {
                log.info("Found contract: {} with type: {}", contract.get_id(), contract.getContractType());
                return ResponseEntity.ok(java.util.Map.of(
                        "success", true,
                        "contract", contract
                ));
            } else {
                log.warn("Contract not found with ID: {}", id);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("Error retrieving contract by ID from Atlas", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ErrorResponse.builder()
                            .error("RETRIEVAL_ERROR")
                            .message("계약서 조회 중 오류가 발생했습니다: " + e.getMessage())
                            .timestamp(LocalDateTime.now().toString())
                            .build());
        }
    }

    // 🟢 MongoDB Atlas에서 사용자별 계약서 조회
    @GetMapping("/api/upload/ocr3/user/{userId}/contracts")
    public ResponseEntity<?> getContractsByUserId(@PathVariable String userId) {
        log.info("Retrieving contracts for userId: {} from MongoDB Atlas", userId);

        try {
            java.util.List<com.superlawva.domain.ocr3.entity.ContractData> contracts =
                    ocrService.getContractsByUserId(userId);

            log.info("Found {} contracts for userId: {} in MongoDB Atlas", contracts.size(), userId);

            return ResponseEntity.ok(java.util.Map.of(
                    "success", true,
                    "userId", userId,
                    "count", contracts.size(),
                    "contracts", contracts
            ));
        } catch (Exception e) {
            log.error("Error retrieving contracts for userId: {} from Atlas", userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ErrorResponse.builder()
                            .error("USER_CONTRACTS_ERROR")
                            .message("사용자 계약서 조회 중 오류가 발생했습니다: " + e.getMessage())
                            .timestamp(LocalDateTime.now().toString())
                            .build());
        }
    }

    // 🟢 사용자 ID 포함 OCR 처리 엔드포인트
    @PostMapping(value = "/api/upload/ocr3/user/{userId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadAndProcessContractWithUserId(
            @PathVariable String userId,
            @RequestParam("file") @NotNull MultipartFile file) {

        log.info("Received OCR request for file: {} with userId: {}", file.getOriginalFilename(), userId);

        try {
            // Validate file
            if (file.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ErrorResponse.builder()
                                .error("EMPTY_FILE")
                                .message("파일이 비어있습니다.")
                                .timestamp(LocalDateTime.now().toString())
                                .build());
            }

            // Process OCR with userId
            OcrResponse response = ocrService.processContractWithUserId(file, userId);

            log.info("OCR processing completed for userId: {} with contract ID: {}",
                    userId, response.getContractData().get_id());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error processing OCR request for userId: {}", userId, e);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ErrorResponse.builder()
                            .error("USER_OCR_ERROR")
                            .message("사용자 OCR 처리 중 오류가 발생했습니다: " + e.getMessage())
                            .timestamp(LocalDateTime.now().toString())
                            .build());
        }
    }


}