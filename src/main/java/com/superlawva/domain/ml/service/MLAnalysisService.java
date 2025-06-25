package com.superlawva.domain.ml.service;

import com.superlawva.domain.ml.client.MLApiClient;
import com.superlawva.domain.ocr3.entity.ContractData;
import com.superlawva.domain.ocr3.repository.ContractDataRepository;
import com.superlawva.domain.document.entity.GeneratedDocument;
import com.superlawva.domain.document.repository.GeneratedDocumentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class MLAnalysisService {

    private final MLApiClient mlApiClient;
    private final ContractDataRepository contractDataRepository;
    private final GeneratedDocumentRepository generatedDocumentRepository;

    /**
     * 계약서 분석 및 결과 저장
     */
    @Transactional
    public GeneratedDocument analyzeContract(String contractId, String userId) {
        log.info("🤖 계약서 분석 시작 - Contract ID: {}, User ID: {}", contractId, userId);

        try {
            // 1. MongoDB에서 계약서 데이터 조회
            ContractData contractData = contractDataRepository.findById(contractId)
                    .orElseThrow(() -> new RuntimeException("계약서를 찾을 수 없습니다: " + contractId));

            // 2. ML API 요청 데이터 구성
            Map<String, Object> mlRequest = buildContractAnalysisRequest(contractData, userId);

            // 3. ML API 호출
            Map<String, Object> mlResponse = mlApiClient.analyzeContract(mlRequest);

            // 4. 분석 결과를 generated_contract 컬렉션에 저장
            GeneratedDocument generatedDocument = saveAnalysisResult(mlResponse, GeneratedDocument.GenerationType.DOCUMENT_MODIFICATION, contractId, userId);

            log.info("✅ 계약서 분석 완료 - Generated Document ID: {}", generatedDocument.getId());
            return generatedDocument;

        } catch (Exception e) {
            log.error("❌ 계약서 분석 실패 - Contract ID: {}", contractId, e);
            throw new RuntimeException("계약서 분석 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * 내용증명서 생성 및 결과 저장
     */
    @Transactional
    public GeneratedDocument generateProofDocument(String contractId, String userId) {
        log.info("📝 내용증명서 생성 시작 - Contract ID: {}, User ID: {}", contractId, userId);

        try {
            // 1. MongoDB에서 계약서 데이터 조회
            ContractData contractData = contractDataRepository.findById(contractId)
                    .orElseThrow(() -> new RuntimeException("계약서를 찾을 수 없습니다: " + contractId));

            // 2. ML API 요청 데이터 구성
            Map<String, Object> mlRequest = buildProofGenerationRequest(contractData, userId);

            // 3. ML API 호출
            Map<String, Object> mlResponse = mlApiClient.generateProofDocument(mlRequest);

            // 4. 생성 결과를 generated_contract 컬렉션에 저장
            GeneratedDocument generatedDocument = saveAnalysisResult(mlResponse, GeneratedDocument.GenerationType.PROOF_CONTENT, contractId, userId);

            log.info("✅ 내용증명서 생성 완료 - Generated Document ID: {}", generatedDocument.getId());
            return generatedDocument;

        } catch (Exception e) {
            log.error("❌ 내용증명서 생성 실패 - Contract ID: {}", contractId, e);
            throw new RuntimeException("내용증명서 생성 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * 특약사항 생성 및 결과 저장
     */
    @Transactional
    public GeneratedDocument generateSpecialTerms(String contractId, String userId) {
        log.info("⚖️ 특약사항 생성 시작 - Contract ID: {}, User ID: {}", contractId, userId);

        try {
            // 1. MongoDB에서 계약서 데이터 조회
            ContractData contractData = contractDataRepository.findById(contractId)
                    .orElseThrow(() -> new RuntimeException("계약서를 찾을 수 없습니다: " + contractId));

            // 2. ML API 요청 데이터 구성
            Map<String, Object> mlRequest = buildSpecialTermsRequest(contractData, userId);

            // 3. ML API 호출
            Map<String, Object> mlResponse = mlApiClient.generateSpecialTerms(mlRequest);

            // 4. 생성 결과를 generated_contract 컬렉션에 저장
            GeneratedDocument generatedDocument = saveAnalysisResult(mlResponse, GeneratedDocument.GenerationType.TEMPLATE_BASED, contractId, userId);

            log.info("✅ 특약사항 생성 완료 - Generated Document ID: {}", generatedDocument.getId());
            return generatedDocument;

        } catch (Exception e) {
            log.error("❌ 특약사항 생성 실패 - Contract ID: {}", contractId, e);
            throw new RuntimeException("특약사항 생성 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * 생성된 문서 결과 조회
     */
    public GeneratedDocument getGeneratedResult(String generatedDocumentId) {
        log.info("📄 생성된 문서 결과 조회 - Generated Document ID: {}", generatedDocumentId);
        
        return generatedDocumentRepository.findById(generatedDocumentId)
                .orElseThrow(() -> new RuntimeException("생성된 문서를 찾을 수 없습니다: " + generatedDocumentId));
    }

    /**
     * ML 서비스 헬스체크
     */
    public boolean healthCheck() {
        try {
            // ML API 서버 연결 상태 확인 (임시 구현)
            return true;
        } catch (Exception e) {
            log.error("❌ ML 서비스 헬스체크 실패", e);
            return false;
        }
    }

    /**
     * 계약서 분석용 ML 요청 데이터 구성
     */
    private Map<String, Object> buildContractAnalysisRequest(ContractData contractData, String userId) {
        Map<String, Object> request = new HashMap<>();
        request.put("contract_id", contractData.getId());
        request.put("user_id", userId);
        request.put("contract_type", contractData.getContractType());

        // 계약서 상세 정보
        Map<String, Object> contractInfo = new HashMap<>();
        contractInfo.put("dates", contractData.getDates());
        contractInfo.put("property", contractData.getProperty());
        contractInfo.put("payment", contractData.getPayment());
        contractInfo.put("lessor", contractData.getLessor());
        contractInfo.put("lessee", contractData.getLessee());
        contractInfo.put("articles", contractData.getArticles());
        contractInfo.put("agreements", contractData.getAgreements());

        request.put("contract_data", contractInfo);

        return request;
    }

    /**
     * 내용증명서 생성용 ML 요청 데이터 구성
     */
    private Map<String, Object> buildProofGenerationRequest(ContractData contractData, String userId) {
        Map<String, Object> request = new HashMap<>();
        request.put("contract_id", contractData.getId());
        request.put("user_id", userId);
        request.put("contract_type", contractData.getContractType());
        request.put("request_type", "PROOF_GENERATION");

        // 계약서 상세 정보
        Map<String, Object> contractInfo = new HashMap<>();
        contractInfo.put("dates", contractData.getDates());
        contractInfo.put("property", contractData.getProperty());
        contractInfo.put("payment", contractData.getPayment());
        contractInfo.put("lessor", contractData.getLessor());
        contractInfo.put("lessee", contractData.getLessee());
        contractInfo.put("articles", contractData.getArticles());
        contractInfo.put("agreements", contractData.getAgreements());

        request.put("contract_data", contractInfo);

        return request;
    }

    /**
     * 특약사항 생성용 ML 요청 데이터 구성
     */
    private Map<String, Object> buildSpecialTermsRequest(ContractData contractData, String userId) {
        Map<String, Object> request = new HashMap<>();
        request.put("contract_id", contractData.getId());
        request.put("user_id", userId);
        request.put("contract_type", contractData.getContractType());
        request.put("request_type", "SPECIAL_TERMS");

        // 계약서 상세 정보
        Map<String, Object> contractInfo = new HashMap<>();
        contractInfo.put("property", contractData.getProperty());
        contractInfo.put("payment", contractData.getPayment());
        contractInfo.put("lessor", contractData.getLessor());
        contractInfo.put("lessee", contractData.getLessee());

        request.put("contract_data", contractInfo);

        return request;
    }

    /**
     * ML 분석 결과를 generated_contract 컬렉션에 저장
     */
    private GeneratedDocument saveAnalysisResult(Map<String, Object> mlResponse, GeneratedDocument.GenerationType generationType, String contractId, String userId) {
        Map<String, Object> additionalMetadata = new HashMap<>();
        additionalMetadata.put("content", extractContent(mlResponse));
        
        GeneratedDocument generatedDocument = GeneratedDocument.builder()
                .userId(Long.parseLong(userId))
                .documentId(contractId)
                .generationType(generationType)
                .requestData(buildRequestDataJson(mlResponse, contractId, userId))
                .generationMetadata(buildGenerationMetadataJson(mlResponse))
                .modelName("Claude-Sonnet-4")
                .modelVersion("v1.0.0")
                .generationTimeSeconds(extractProcessingTime(mlResponse))
                .status(GeneratedDocument.DocumentStatus.GENERATED)
                .additionalMetadata(additionalMetadata)
                .createdAt(LocalDateTime.now(ZoneOffset.UTC))
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC))
                .build();

        return generatedDocumentRepository.save(generatedDocument);
    }

    /**
     * 요청 데이터 JSON 구성
     */
    private String buildRequestDataJson(Map<String, Object> mlResponse, String contractId, String userId) {
        Map<String, Object> requestData = new HashMap<>();
        requestData.put("contract_id", contractId);
        requestData.put("user_id", userId);
        requestData.put("timestamp", System.currentTimeMillis());
        
        // JSON 문자열로 변환 (실제 구현에서는 ObjectMapper 사용)
        return requestData.toString();
    }

    /**
     * 생성 메타데이터 JSON 구성
     */
    private String buildGenerationMetadataJson(Map<String, Object> mlResponse) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("model_version", "v1.0.0");
        metadata.put("generation_timestamp", System.currentTimeMillis());
        metadata.put("response_format", "json");
        
        // JSON 문자열로 변환 (실제 구현에서는 ObjectMapper 사용)
        return metadata.toString();
    }

    /**
     * ML 응답에서 처리 시간 추출
     */
    private Double extractProcessingTime(Map<String, Object> mlResponse) {
        try {
            if (mlResponse.containsKey("processing_time")) {
                return Double.valueOf(mlResponse.get("processing_time").toString());
            }
            return 0.0;
        } catch (Exception e) {
            log.warn("처리 시간 추출 실패", e);
            return 0.0;
        }
    }

    /**
     * ML 응답에서 콘텐츠 추출
     */
    private String extractContent(Map<String, Object> mlResponse) {
        try {
            if (mlResponse.containsKey("content")) {
                return mlResponse.get("content").toString();
            }
            if (mlResponse.containsKey("result")) {
                return mlResponse.get("result").toString();
            }
            return "분석 결과가 없습니다.";
        } catch (Exception e) {
            log.warn("콘텐츠 추출 실패", e);
            return "콘텐츠 추출 중 오류가 발생했습니다.";
        }
    }
}