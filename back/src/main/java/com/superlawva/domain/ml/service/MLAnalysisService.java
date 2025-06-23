//package com.superlawva.domain.ml.service;
//
//import com.superlawva.domain.ml.client.MLApiClient;
//import com.superlawva.domain.ml.dto.MLAnalysisRequest;
//import com.superlawva.domain.ml.dto.MLAnalysisResponse;
//import com.superlawva.domain.ocr3.entity.ContractData;
//import com.superlawva.domain.ocr3.repository.ContractDataRepository;
//import com.superlawva.domain.document.entity.GeneratedDocument;
//import com.superlawva.domain.document.repository.GeneratedDocumentRepository;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.time.LocalDateTime;
//import java.time.ZoneOffset;
//
//@Slf4j
//@Service
//@RequiredArgsConstructor
//public class MLAnalysisService {
//
//    private final MLApiClient mlApiClient;
//    private final ContractDataRepository contractDataRepository;
//    private final GeneratedDocumentRepository generatedDocumentRepository;
//
//    /**
//     * 계약서 분석 및 결과 저장
//     */
//    @Transactional
//    public GeneratedDocument analyzeContract(String contractId, String userId) {
//        log.info("🤖 계약서 분석 시작 - Contract ID: {}, User ID: {}", contractId, userId);
//
//        try {
//            // 1. MongoDB에서 계약서 데이터 조회
//            ContractData contractData = contractDataRepository.findById(contractId)
//                    .orElseThrow(() -> new RuntimeException("계약서를 찾을 수 없습니다: " + contractId));
//
//            // 2. ML API 요청 데이터 구성
//            MLAnalysisRequest mlRequest = buildContractAnalysisRequest(contractData, userId);
//
//            // 3. ML API 호출
//            MLAnalysisResponse mlResponse = mlApiClient.analyzeContract(mlRequest);
//
//            // 4. 분석 결과를 generated_contract 컬렉션에 저장
//            GeneratedDocument generatedDocument = saveAnalysisResult(mlResponse, "CONTRACT_ANALYSIS");
//
//            log.info("✅ 계약서 분석 완료 - Generated Document ID: {}", generatedDocument.getId());
//            return generatedDocument;
//
//        } catch (Exception e) {
//            log.error("❌ 계약서 분석 실패 - Contract ID: {}", contractId, e);
//            throw new RuntimeException("계약서 분석 중 오류가 발생했습니다: " + e.getMessage());
//        }
//    }
//
//    /**
//     * 내용증명서 생성 및 결과 저장
//     */
//    @Transactional
//    public GeneratedDocument generateProofDocument(String contractId, String userId) {
//        log.info("📝 내용증명서 생성 시작 - Contract ID: {}, User ID: {}", contractId, userId);
//
//        try {
//            // 1. MongoDB에서 계약서 데이터 조회
//            ContractData contractData = contractDataRepository.findById(contractId)
//                    .orElseThrow(() -> new RuntimeException("계약서를 찾을 수 없습니다: " + contractId));
//
//            // 2. ML API 요청 데이터 구성
//            MLAnalysisRequest mlRequest = buildProofGenerationRequest(contractData, userId);
//
//            // 3. ML API 호출
//            MLAnalysisResponse mlResponse = mlApiClient.generateProofDocument(mlRequest);
//
//            // 4. 생성 결과를 generated_contract 컬렉션에 저장
//            GeneratedDocument generatedDocument = saveAnalysisResult(mlResponse, "PROOF_GENERATION");
//
//            log.info("✅ 내용증명서 생성 완료 - Generated Document ID: {}", generatedDocument.getId());
//            return generatedDocument;
//
//        } catch (Exception e) {
//            log.error("❌ 내용증명서 생성 실패 - Contract ID: {}", contractId, e);
//            throw new RuntimeException("내용증명서 생성 중 오류가 발생했습니다: " + e.getMessage());
//        }
//    }
//
//    /**
//     * 계약서 분석용 ML 요청 데이터 구성
//     */
//    private MLAnalysisRequest buildContractAnalysisRequest(ContractData contractData, String userId) {
//        return MLAnalysisRequest.builder()
//                .contractId(contractData.getId())
//                .userId(userId)
//                .requestType("CONTRACT_ANALYSIS")
//                .contractData(MLAnalysisRequest.ContractDataForML.builder()
//                        .contractType(contractData.getContractType())
//                        .dates(contractData.getDates())
//                        .property(contractData.getProperty())
//                        .payment(contractData.getPayment())
//                        .lessor(contractData.getLessor())
//                        .lessee(contractData.getLessee())
//                        .articles(contractData.getArticles())
//                        .agreements(contractData.getAgreements())
//                        .build())
//                .additionalParams(MLAnalysisRequest.AdditionalParams.builder()
//                        .analysisType("COMPREHENSIVE")
//                        .outputFormat("DETAILED")
//                        .language("ko")
//                        .priority("NORMAL")
//                        .build())
//                .build();
//    }
//
//    /**
//     * 내용증명서 생성용 ML 요청 데이터 구성
//     */
//    private MLAnalysisRequest buildProofGenerationRequest(ContractData contractData, String userId) {
//        return MLAnalysisRequest.builder()
//                .contractId(contractData.getId())
//                .userId(userId)
//                .requestType("PROOF_GENERATION")
//                .contractData(MLAnalysisRequest.ContractDataForML.builder()
//                        .contractType(contractData.getContractType())
//                        .dates(contractData.getDates())
//                        .property(contractData.getProperty())
//                        .payment(contractData.getPayment())
//                        .lessor(contractData.getLessor())
//                        .lessee(contractData.getLessee())
//                        .articles(contractData.getArticles())
//                        .agreements(contractData.getAgreements())
//                        .build())
//                .additionalParams(MLAnalysisRequest.AdditionalParams.builder()
//                        .analysisType("LEGAL_REVIEW")
//                        .outputFormat("REPORT")
//                        .language("ko")
//                        .priority("HIGH")
//                        .build())
//                .build();
//    }
//
//    /**
//     * ML 분석 결과를 generated_contract 컬렉션에 저장
//     */
//    private GeneratedDocument saveAnalysisResult(MLAnalysisResponse mlResponse, String generationType) {
//        GeneratedDocument generatedDocument = GeneratedDocument.builder()
//                .userId(mlResponse.getUserId())
//                .documentId(mlResponse.getContractId())
//                .generationType(generationType)
//                .requestData(buildRequestDataJson(mlResponse))
//                .generationMetadata(buildGenerationMetadataJson(mlResponse))
//                .modelName(mlResponse.getProcessingInfo() != null ?
//                          mlResponse.getProcessingInfo().getModelName() : "ML-Engine-v1")
//                .modelVersion(mlResponse.getProcessingInfo() != null ?
//                            mlResponse.getProcessingInfo().getModelVersion() : "1.0.0")
//                .generationTimeSeconds(mlResponse.getProcessingInfo() != null ?
//                                     mlResponse.getProcessingInfo().getProcessingTimeSeconds() : 0.0)
//                .tokenCount(mlResponse.getProcessingInfo() != null ?
//                          mlResponse.getProcessingInfo().getTokenCount() : 0)
//                .qualityScore(mlResponse.getProcessingInfo() != null ?
//                            mlResponse.getProcessingInfo().getQualityScore() : 0.0)
//                .status("GENERATED")
//                .createdAt(LocalDateTime.now(ZoneOffset.UTC))
//                .updatedAt(LocalDateTime.now(ZoneOffset.UTC))
//                .build();
//
//        return generatedDocumentRepository.save(generatedDocument);
//    }
//
//    /**
//     * 요청 데이터 JSON 문자열 생성
//     */
//    private String buildRequestDataJson(MLAnalysisResponse mlResponse) {
//        return String.format("""
//            {
//                "contractId": "%s",
//                "userId": "%s",
//                "requestType": "%s",
//                "timestamp": "%s"
//            }
//            """,
//            mlResponse.getContractId(),
//            mlResponse.getUserId(),
//            mlResponse.getRequestType(),
//            LocalDateTime.now(ZoneOffset.UTC)
//        );
//    }
//
//    /**
//     * 생성 메타데이터 JSON 문자열 생성
//     */
//    private String buildGenerationMetadataJson(MLAnalysisResponse mlResponse) {
//        StringBuilder metadata = new StringBuilder();
//        metadata.append("{");
//
//        if (mlResponse.getAnalysisResult() != null) {
//            metadata.append(String.format("""
//                "analysisResult": {
//                    "riskScore": %s,
//                    "riskLevel": "%s",
//                    "summary": "%s"
//                },
//                """,
//                mlResponse.getAnalysisResult().getRiskScore(),
//                mlResponse.getAnalysisResult().getRiskLevel(),
//                mlResponse.getAnalysisResult().getSummary()
//            ));
//        }
//
//        if (mlResponse.getGeneratedContent() != null) {
//            metadata.append(String.format("""
//                "generatedContent": {
//                    "contentType": "%s",
//                    "title": "%s",
//                    "confidenceScore": %s
//                },
//                """,
//                mlResponse.getGeneratedContent().getContentType(),
//                mlResponse.getGeneratedContent().getTitle(),
//                mlResponse.getGeneratedContent().getConfidenceScore()
//            ));
//        }
//
//        metadata.append(String.format("""
//            "processedAt": "%s",
//            "success": %s
//            """,
//            LocalDateTime.now(ZoneOffset.UTC),
//            mlResponse.isSuccess()
//        ));
//
//        metadata.append("}");
//        return metadata.toString();
//    }
//}