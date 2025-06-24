package com.superlawva.domain.document.service;

import com.superlawva.domain.document.dto.ProofContentRequestDTO;
import com.superlawva.domain.document.dto.DocumentResponseDTO;
import com.superlawva.domain.document.entity.Document;
import com.superlawva.domain.document.entity.GeneratedDocument;
import com.superlawva.domain.document.repository.DocumentRepository;
import com.superlawva.domain.document.repository.GeneratedDocumentRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ProofContentService {

    private final DocumentRepository documentRepository;
    private final GeneratedDocumentRepository generatedDocumentRepository;
    // TODO: AI 모델 서비스 주입 (MLOps 팀 구현 후)
    // private final AiModelService aiModelService;

    /**
     * AI 기반 증명 내용 생성
     */
    public DocumentResponseDTO generateProofContent(String contractDocumentId, ProofContentRequestDTO request) {
        log.info("AI 증명 내용 생성 시작 - 계약서 ID: {}, 증명 유형: {}", 
                contractDocumentId, request.getProofType());
        
        long startTime = System.currentTimeMillis();
        
        try {
            // 1. 기준 계약서 문서 조회
            Document contractDocument = documentRepository.findById(contractDocumentId)
                    .orElseThrow(() -> new RuntimeException("계약서 문서를 찾을 수 없습니다: " + contractDocumentId));
            
            // 2. 증명서 Document 엔티티 생성
            Document proofDocument = Document.builder()
                .userId(request.getUserId())
                .originalFilename(generateProofFilename(request))
                .encryptedFileKey(UUID.randomUUID().toString())
                .documentType(Document.DocumentType.CERTIFICATE)
                .mimeType("application/pdf")
                .fileSizeBytes(0L) // 생성 후 업데이트
                .status(Document.DocumentStatus.OCR_PROCESSING) // 생성 중
                .build();
            
            proofDocument = documentRepository.save(proofDocument);
            
                         // 3. GeneratedDocument 메타데이터 생성
             GeneratedDocument generatedDocument = GeneratedDocument.builder()
                 .userId(request.getUserId())
                 .documentId(proofDocument.getId()) // MongoDB ObjectId 참조
                 .generationType(GeneratedDocument.GenerationType.PROOF_CONTENT)
                 .requestData(convertRequestToJson(request, contractDocumentId))
                 .modelName("proof-generator-v1") // TODO: 실제 모델명으로 변경
                 .modelVersion("1.0.0")
                 .status(GeneratedDocument.DocumentStatus.GENERATING)
                 .build();
            
            generatedDocument = generatedDocumentRepository.save(generatedDocument);
            
            // 4. 계약서 내용 분석 및 증명서 생성
            String contractContent = extractContractContent(contractDocument); // TODO: 구현
            String proofContent = generateProofContentWithAI(contractContent, request);
            
            // 5. 생성 결과 저장 및 메타데이터 업데이트
            long endTime = System.currentTimeMillis();
            double generationTime = (endTime - startTime) / 1000.0;
            
            // 문서 상태 업데이트
            proofDocument.setStatus(Document.DocumentStatus.OCR_COMPLETED);
            proofDocument.setFileSizeBytes((long) proofContent.length());
            documentRepository.save(proofDocument);
            
            // 생성 메타데이터 업데이트
            generatedDocument.setStatus(GeneratedDocument.DocumentStatus.GENERATED);
            generatedDocument.setGenerationTimeSeconds(generationTime);
            generatedDocument.setTokenCount(estimateTokenCount(proofContent));
            generatedDocument.setQualityScore(calculateProofQualityScore(proofContent, request));
            generatedDocument.setGenerationMetadata(buildGenerationMetadata(request, generationTime));
            generatedDocumentRepository.save(generatedDocument);
            
            log.info("AI 증명 내용 생성 완료 - Document ID: {}, 생성 시간: {}초", 
                    proofDocument.getId(), generationTime);
            
            return DocumentResponseDTO.fromEntity(proofDocument);
            
        } catch (Exception e) {
            log.error("AI 증명 내용 생성 실패: {}", e.getMessage(), e);
            throw new RuntimeException("증명 내용 생성 실패: " + e.getMessage(), e);
        }
    }

    // ==================== Private Methods ====================

    private String generateProofFilename(ProofContentRequestDTO request) {
        return String.format("AI생성_%s_%s.pdf", 
                request.getProofType(), 
                LocalDate.now().toString());
    }

    private String convertRequestToJson(ProofContentRequestDTO request, String contractDocumentId) {
        // TODO: JSON 직렬화 구현
        return String.format("{\"contractDocumentId\": \"%s\", \"proofType\": \"%s\"}", 
                contractDocumentId, request.getProofType());
    }

    private String extractContractContent(Document contractDocument) {
        // TODO: 실제 계약서 내용 추출 로직 구현
        log.info("계약서 내용 추출 - Document ID: {}", contractDocument.getId());
        return "계약서 내용 추출 결과 (임시)";
    }

    private String generateProofContentWithAI(String contractContent, ProofContentRequestDTO request) {
        // TODO: MLOps 팀의 AI 모델 호출
        log.info("AI 모델 호출 - 증명서 생성 (임시 구현)");
        
        LocalDate issueDate = request.getIssueDate() != null ? request.getIssueDate() : LocalDate.now();
        
        return switch (request.getProofType()) {
            case "거주확인서" -> generateResidenceProof(contractContent, request, issueDate);
            case "계약확인서" -> generateContractProof(contractContent, request, issueDate);
            case "임대차현황확인서" -> generateLeaseStatusProof(contractContent, request, issueDate);
            default -> generateGenericProof(contractContent, request, issueDate);
        };
    }

    private String generateResidenceProof(String contractContent, ProofContentRequestDTO request, LocalDate issueDate) {
        return String.format("""
                거주확인서 (AI 생성)
                
                발행일: %s
                발행목적: %s
                
                [거주 확인 내용]
                위 계약서를 바탕으로 거주 사실을 확인합니다.
                
                발행자: %s
                직책: %s
                
                ※ 본 증명서는 AI에 의해 생성되었습니다.
                """, 
                issueDate, 
                request.getPurpose(),
                request.getIssuerName() != null ? request.getIssuerName() : "미기재",
                request.getIssuerTitle() != null ? request.getIssuerTitle() : "미기재");
    }

    private String generateContractProof(String contractContent, ProofContentRequestDTO request, LocalDate issueDate) {
        return String.format("""
                계약확인서 (AI 생성)
                
                발행일: %s
                발행목적: %s
                
                [계약 확인 내용]
                위 계약서의 유효성을 확인합니다.
                
                증명기간: %s ~ %s
                
                발행자: %s
                직책: %s
                
                ※ 본 증명서는 AI에 의해 생성되었습니다.
                """, 
                issueDate, 
                request.getPurpose(),
                request.getPeriodStartDate() != null ? request.getPeriodStartDate() : "미기재",
                request.getPeriodEndDate() != null ? request.getPeriodEndDate() : "미기재",
                request.getIssuerName() != null ? request.getIssuerName() : "미기재",
                request.getIssuerTitle() != null ? request.getIssuerTitle() : "미기재");
    }

    private String generateLeaseStatusProof(String contractContent, ProofContentRequestDTO request, LocalDate issueDate) {
        return String.format("""
                임대차현황확인서 (AI 생성)
                
                발행일: %s
                발행목적: %s
                
                [임대차 현황]
                현재 임대차 계약 상태를 확인합니다.
                
                증명기간: %s ~ %s
                
                발행자: %s
                직책: %s
                
                ※ 본 증명서는 AI에 의해 생성되었습니다.
                """, 
                issueDate, 
                request.getPurpose(),
                request.getPeriodStartDate() != null ? request.getPeriodStartDate() : "미기재",
                request.getPeriodEndDate() != null ? request.getPeriodEndDate() : "미기재",
                request.getIssuerName() != null ? request.getIssuerName() : "미기재",
                request.getIssuerTitle() != null ? request.getIssuerTitle() : "미기재");
    }

    private String generateGenericProof(String contractContent, ProofContentRequestDTO request, LocalDate issueDate) {
        return String.format("""
                %s (AI 생성)
                
                발행일: %s
                발행목적: %s
                
                [증명 내용]
                요청하신 증명서를 발행합니다.
                
                발행자: %s
                직책: %s
                
                ※ 본 증명서는 AI에 의해 생성되었습니다.
                """, 
                request.getProofType(),
                issueDate, 
                request.getPurpose(),
                request.getIssuerName() != null ? request.getIssuerName() : "미기재",
                request.getIssuerTitle() != null ? request.getIssuerTitle() : "미기재");
    }

    private Integer estimateTokenCount(String content) {
        return content.length() / 2;
    }

    private Integer calculateProofQualityScore(String content, ProofContentRequestDTO request) {
        int score = 75; // 기본 점수
        
        if (content.contains("발행일")) score += 5;
        if (content.contains("발행목적")) score += 5;
        if (content.contains("발행자")) score += 5;
        if (request.getRequiredFields() != null && !request.getRequiredFields().isEmpty()) {
            score += 10;
        }
        
        return Math.min(score, 100);
    }

    private String buildGenerationMetadata(ProofContentRequestDTO request, double generationTime) {
        return String.format("{\"generationTime\": %.2f, \"proofType\": \"%s\", \"purpose\": \"%s\"}", 
                generationTime, request.getProofType(), request.getPurpose());
    }
} 