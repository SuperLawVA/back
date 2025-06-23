package com.superlawva.domain.document.service;

import com.superlawva.domain.document.dto.ContractGenerationRequestDTO;
import com.superlawva.domain.document.dto.DocumentResponseDTO;
import com.superlawva.domain.document.entity.Document;
import com.superlawva.domain.document.entity.GeneratedDocument;
import com.superlawva.domain.document.repository.DocumentRepository;
import com.superlawva.domain.document.repository.GeneratedDocumentRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ContractGenerationService {

    private final DocumentRepository documentRepository;
    private final GeneratedDocumentRepository generatedDocumentRepository;
    // TODO: AI 모델 서비스 주입 (MLOps 팀 구현 후)
    // private final AiModelService aiModelService;

    /**
     * AI 기반 계약서 생성
     */
    public DocumentResponseDTO generateContract(ContractGenerationRequestDTO request) {
        log.info("AI 계약서 생성 시작 - 사용자 ID: {}, 계약 유형: {}", 
                request.getUserId(), request.getContractType());
        
        long startTime = System.currentTimeMillis();
        
        try {
            // 1. 기본 Document 엔티티 생성
            Document document = Document.builder()
                .userId(request.getUserId())
                .originalFilename(generateContractFilename(request))
                .encryptedFileKey(UUID.randomUUID().toString())
                .documentType(mapContractTypeToDocumentType(request.getContractType()))
                .mimeType("application/pdf")
                .fileSizeBytes(0L) // 생성 후 업데이트
                .status(Document.DocumentStatus.OCR_PROCESSING) // 생성 중
                .build();
            
            document = documentRepository.save(document);
            
            // 2. GeneratedDocument 메타데이터 생성
            GeneratedDocument generatedDocument = GeneratedDocument.builder()
                .userId(request.getUserId())
                .documentId(document.getId()) // MongoDB ObjectId 참조
                .generationType(GeneratedDocument.GenerationType.CONTRACT_GENERATION)
                .requestData(convertRequestToJson(request)) // TODO: JSON 변환 구현
                .modelName("contract-generator-v1") // TODO: 실제 모델명으로 변경
                .modelVersion("1.0.0")
                .status(GeneratedDocument.DocumentStatus.GENERATING)
                .build();
            
            generatedDocument = generatedDocumentRepository.save(generatedDocument);
            
            // 3. AI 모델 호출하여 계약서 생성
            // TODO: MLOps 팀의 AI 모델 연동
            String generatedContent = generateContractWithAI(request);
            
            // 4. 생성 결과 저장 및 메타데이터 업데이트
            long endTime = System.currentTimeMillis();
            double generationTime = (endTime - startTime) / 1000.0;
            
            // 문서 상태 업데이트
            document.setStatus(Document.DocumentStatus.OCR_COMPLETED);
            document.setFileSizeBytes((long) generatedContent.length());
            documentRepository.save(document);
            
            // 생성 메타데이터 업데이트
            generatedDocument.setStatus(GeneratedDocument.DocumentStatus.GENERATED);
            generatedDocument.setGenerationTimeSeconds(generationTime);
            generatedDocument.setTokenCount(estimateTokenCount(generatedContent));
            generatedDocument.setQualityScore(calculateQualityScore(generatedContent));
            generatedDocument.setGenerationMetadata(buildGenerationMetadata(request, generationTime));
            generatedDocumentRepository.save(generatedDocument);
            
            log.info("AI 계약서 생성 완료 - Document ID: {}, 생성 시간: {}초", 
                    document.getId(), generationTime);
            
            return DocumentResponseDTO.fromEntity(document);
            
        } catch (Exception e) {
            log.error("AI 계약서 생성 실패: {}", e.getMessage(), e);
            // TODO: 실패 상태 업데이트 로직
            throw new RuntimeException("계약서 생성 실패: " + e.getMessage(), e);
        }
    }

    /**
     * 사용자의 생성된 문서 목록 조회
     */
    @Transactional(readOnly = true)
    public List<DocumentResponseDTO> getGeneratedDocuments(Long userId) {
        List<GeneratedDocument> generatedDocs = generatedDocumentRepository
                .findByUserIdOrderByCreatedAtDesc(userId);
        
        return generatedDocs.stream()
                .map(genDoc -> {
                    Document document = documentRepository.findById(genDoc.getDocumentId())
                            .orElse(null);
                    return document != null ? DocumentResponseDTO.fromEntity(document) : null;
                })
                .filter(doc -> doc != null)
                .collect(Collectors.toList());
    }

    /**
     * 생성된 문서 상세 조회 (메타데이터 포함)
     */
    @Transactional(readOnly = true)
    public DocumentResponseDTO getGeneratedDocument(String documentId) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found: " + documentId));
        
        // TODO: GeneratedDocument 정보도 포함한 확장된 DTO 반환
        return DocumentResponseDTO.fromEntity(document);
    }

    // ==================== Private Methods ====================

    private String generateContractFilename(ContractGenerationRequestDTO request) {
        return String.format("AI생성_%s계약서_%s.pdf", 
                request.getContractType(), 
                LocalDateTime.now().toString().substring(0, 10));
    }

    private Document.DocumentType mapContractTypeToDocumentType(String contractType) {
        return switch (contractType) {
            case "전세" -> Document.DocumentType.LEASE_JEONSE;
            case "월세" -> Document.DocumentType.LEASE_MONTHLY;
            default -> Document.DocumentType.OTHER;
        };
    }

    private String convertRequestToJson(ContractGenerationRequestDTO request) {
        // TODO: JSON 직렬화 구현
        return "{}"; // 임시
    }

    private String generateContractWithAI(ContractGenerationRequestDTO request) {
        // TODO: MLOps 팀의 AI 모델 호출
        log.info("AI 모델 호출 - 계약서 생성 (임시 구현)");
        
        return String.format("""
                임대차계약서 (AI 생성)
                
                계약 유형: %s
                부동산 소재지: %s
                보증금: %s원
                월세: %s원
                계약기간: %s ~ %s
                
                [임시 생성된 계약서]
                본 계약서는 AI에 의해 생성되었습니다.
                실제 법적 효력을 위해서는 전문가 검토가 필요합니다.
                """, 
                request.getContractType(),
                request.getPropertyAddress(),
                request.getDepositAmount(),
                request.getMonthlyRent(),
                request.getContractStartDate(),
                request.getContractEndDate());
    }

    private Integer estimateTokenCount(String content) {
        // 간단한 토큰 수 추정 (한글 기준)
        return content.length() / 2;
    }

    private Integer calculateQualityScore(String content) {
        // 임시 품질 점수 계산 로직
        int score = 70; // 기본 점수
        
        if (content.length() > 1000) score += 10;
        if (content.contains("특약")) score += 5;
        if (content.contains("조항")) score += 5;
        
        return Math.min(score, 100);
    }

    private String buildGenerationMetadata(ContractGenerationRequestDTO request, double generationTime) {
        // TODO: 상세한 메타데이터 JSON 생성
        return String.format("{\"generationTime\": %.2f, \"contractType\": \"%s\"}", 
                generationTime, request.getContractType());
    }
} 