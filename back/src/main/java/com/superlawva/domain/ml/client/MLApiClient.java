//package com.superlawva.domain.ml.client;
//
//import com.superlawva.domain.ml.dto.MLAnalysisRequest;
//import com.superlawva.domain.ml.dto.MLAnalysisResponse;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.http.*;
//import org.springframework.stereotype.Component;
//import org.springframework.web.client.RestTemplate;
//
//@Slf4j
//@Component
//@RequiredArgsConstructor
//public class MLApiClient {
//
//    private final RestTemplate restTemplate;
//
//    @Value("${ml.api.base-url:http://localhost:8000}")
//    private String mlApiBaseUrl;
//
//    @Value("${ml.api.timeout:60000}")
//    private int timeout;
//
//    /**
//     * 계약서 분석 요청
//     */
//    public MLAnalysisResponse analyzeContract(MLAnalysisRequest request) {
//        log.info("🤖 ML API 계약서 분석 요청 시작 - Contract ID: {}", request.getContractId());
//
//        try {
//            String url = mlApiBaseUrl + "/api/v1/analyze/contract";
//
//            HttpHeaders headers = new HttpHeaders();
//            headers.setContentType(MediaType.APPLICATION_JSON);
//            headers.set("User-Agent", "SuperLawva-Backend/1.0");
//
//            HttpEntity<MLAnalysisRequest> entity = new HttpEntity<>(request, headers);
//
//            long startTime = System.currentTimeMillis();
//            ResponseEntity<MLAnalysisResponse> response = restTemplate.postForEntity(
//                url, entity, MLAnalysisResponse.class
//            );
//
//            double processingTime = (System.currentTimeMillis() - startTime) / 1000.0;
//            log.info("✅ ML API 계약서 분석 완료 - 처리시간: {}초", processingTime);
//
//            return response.getBody();
//
//        } catch (Exception e) {
//            log.error("❌ ML API 계약서 분석 실패", e);
//            throw new RuntimeException("ML 계약서 분석 중 오류가 발생했습니다: " + e.getMessage());
//        }
//    }
//
//    /**
//     * 내용증명서 생성 요청
//     */
//    public MLAnalysisResponse generateProofDocument(MLAnalysisRequest request) {
//        log.info("📝 ML API 내용증명서 생성 요청 시작 - Contract ID: {}", request.getContractId());
//
//        try {
//            String url = mlApiBaseUrl + "/api/v1/generate/proof";
//
//            HttpHeaders headers = new HttpHeaders();
//            headers.setContentType(MediaType.APPLICATION_JSON);
//            headers.set("User-Agent", "SuperLawva-Backend/1.0");
//
//            HttpEntity<MLAnalysisRequest> entity = new HttpEntity<>(request, headers);
//
//            long startTime = System.currentTimeMillis();
//            ResponseEntity<MLAnalysisResponse> response = restTemplate.postForEntity(
//                url, entity, MLAnalysisResponse.class
//            );
//
//            double processingTime = (System.currentTimeMillis() - startTime) / 1000.0;
//            log.info("✅ ML API 내용증명서 생성 완료 - 처리시간: {}초", processingTime);
//
//            return response.getBody();
//
//        } catch (Exception e) {
//            log.error("❌ ML API 내용증명서 생성 실패", e);
//            throw new RuntimeException("ML 내용증명서 생성 중 오류가 발생했습니다: " + e.getMessage());
//        }
//    }
//}