package com.superlawva.domain.document.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.superlawva.domain.document.dto.ContractGenerationRequestDTO;
import com.superlawva.domain.document.dto.ProofContentRequestDTO;
import com.superlawva.domain.document.dto.DocumentResponseDTO;
import com.superlawva.domain.document.service.ContractGenerationService;
import com.superlawva.domain.document.service.ProofContentService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/generate")
@RequiredArgsConstructor
@Tag(name = "📝 Document Generation", description = "문서 생성 API")
@SecurityRequirement(name = "Bearer Authentication")
@Slf4j
public class DocumentGenerationController {

    private final ContractGenerationService contractGenerationService;
    private final ProofContentService proofContentService;

    @Operation(
        summary = "🚀 AI 계약서 생성 API",
        description = """
            사용자 요구사항을 기반으로 AI가 맞춤형 계약서를 생성합니다.
            
            **처리 과정:**
            1. 사용자 입력 분석 (계약 유형, 조건 등)
            2. 법적 조항 자동 생성
            3. 맞춤형 특약사항 추천
            4. 완성된 계약서 문서 반환
            
            **예상 입력:**
            - 계약 유형 (전세/월세)
            - 부동산 정보
            - 계약 조건
            - 특별 요구사항
            """
    )
    @PostMapping("/contract")
    public ResponseEntity<DocumentResponseDTO> generateContract(
            @Valid @RequestBody ContractGenerationRequestDTO request) {
        
        log.info("AI 계약서 생성 요청 - 사용자 ID: {}, 계약 유형: {}", 
                request.getUserId(), request.getContractType());
        
        // TODO: MLOps 팀의 AI 모델 연동 후 구현
        DocumentResponseDTO response = contractGenerationService.generateContract(request);
        
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "📋 AI 증명 내용 생성 API",
        description = """
            업로드된 계약서를 기반으로 사용자 요구사항에 맞는 증명 내용을 생성합니다.
            
            **처리 과정:**
            1. 기존 계약서 내용 분석
            2. 사용자 요구사항 파악
            3. 증명서류 자동 생성 (거주확인서, 계약확인서 등)
            4. 법적 유효성 검증
            
            **활용 예시:**
            - 거주확인서 생성
            - 계약 내용 증명서
            - 임대차 현황 확인서
            """
    )
    @PostMapping("/proof-content")
    public ResponseEntity<DocumentResponseDTO> generateProofContent(
            @Parameter(description = "기준이 되는 계약서 문서 ID")
            @RequestParam("contractDocumentId") String contractDocumentId,
            
            @Valid @RequestBody ProofContentRequestDTO request) {
        
        log.info("AI 증명 내용 생성 요청 - 사용자 ID: {}, 계약서 ID: {}, 증명 유형: {}", 
                request.getUserId(), contractDocumentId, request.getProofType());
        
        // TODO: MLOps 팀의 AI 모델 연동 후 구현
        DocumentResponseDTO response = proofContentService.generateProofContent(contractDocumentId, request);
        
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "📄 생성된 문서 목록 조회",
        description = "사용자가 AI로 생성한 모든 문서의 목록을 조회합니다."
    )
    @GetMapping("/documents")
    public ResponseEntity<List<DocumentResponseDTO>> getGeneratedDocuments(
            @RequestParam Long userId) {
        
        // TODO: 생성된 문서만 필터링하는 로직 구현
        List<DocumentResponseDTO> documents = contractGenerationService.getGeneratedDocuments(userId);
        
        return ResponseEntity.ok(documents);
    }

    @Operation(
        summary = "📄 생성된 문서 상세 조회",
        description = "특정 생성 문서의 상세 정보와 생성 과정을 조회합니다."
    )
    @GetMapping("/documents/{documentId}")
    public ResponseEntity<DocumentResponseDTO> getGeneratedDocument(
            @PathVariable String documentId) {
        
        // TODO: 생성 메타데이터 포함한 상세 정보 반환
        DocumentResponseDTO document = contractGenerationService.getGeneratedDocument(documentId);
        
        return ResponseEntity.ok(document);
    }
} 