//package com.superlawva.domain.ml.client;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.http.*;
//import org.springframework.stereotype.Component;
//import org.springframework.web.client.RestTemplate;
//
//import java.util.HashMap;
//import java.util.Map;
//
//@Slf4j
//@Component
//@RequiredArgsConstructor
//public class MLApiClient {
//
//    private final RestTemplate restTemplate;
//    private final ObjectMapper objectMapper;
//
//    @Value("${ml.api.base-url:http://3.34.41.104:8000}")
//    private String mlApiBaseUrl;
//
//    @Value("${ml.api.timeout:60000}")
//    private int timeout;
//
//    /**
//     * 계약서 분석 요청 (실제 ML API 엔드포인트: /api/v1/analyze-contract)
//     */
//    public Map<String, Object> analyzeContract(Map<String, Object> contractData) {
//        log.info("🤖 ML API 계약서 분석 요청 시작 - Contract ID: {}", contractData.get("contract_id"));
//
//        try {
//            String url = mlApiBaseUrl + "/api/v1/analyze-contract";
//
//            HttpHeaders headers = new HttpHeaders();
//            headers.setContentType(MediaType.APPLICATION_JSON);
//            headers.set("User-Agent", "SuperLawva-Backend/1.0");
//
//            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(contractData, headers);
//
//            long startTime = System.currentTimeMillis();
//            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);
//
//            double processingTime = (System.currentTimeMillis() - startTime) / 1000.0;
//            log.info("✅ ML API 계약서 분석 완료 - 처리시간: {}초", processingTime);
//
//            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
//                return response.getBody();
//            } else {
//                throw new RuntimeException("ML API 응답 오류: " + response.getStatusCode());
//            }
//
//        } catch (Exception e) {
//            log.error("❌ ML API 계약서 분석 실패", e);
//            throw new RuntimeException("ML 계약서 분석 중 오류가 발생했습니다: " + e.getMessage());
//        }
//    }
//
//    /**
//     * 내용증명서 생성 요청 (실제 ML API 엔드포인트: /api/v1/generate-letter)
//     */
//    public Map<String, Object> generateProofDocument(Map<String, Object> contractData) {
//        log.info("📝 ML API 내용증명서 생성 요청 시작 - Contract ID: {}", contractData.get("contract_id"));
//
//        try {
//            String url = mlApiBaseUrl + "/api/v1/generate-letter";
//
//            HttpHeaders headers = new HttpHeaders();
//            headers.setContentType(MediaType.APPLICATION_JSON);
//            headers.set("User-Agent", "SuperLawva-Backend/1.0");
//
//            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(contractData, headers);
//
//            long startTime = System.currentTimeMillis();
//            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);
//
//            double processingTime = (System.currentTimeMillis() - startTime) / 1000.0;
//            log.info("✅ ML API 내용증명서 생성 완료 - 처리시간: {}초", processingTime);
//
//            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
//                return response.getBody();
//            } else {
//                throw new RuntimeException("ML API 응답 오류: " + response.getStatusCode());
//            }
//
//        } catch (Exception e) {
//            log.error("❌ ML API 내용증명서 생성 실패", e);
//            throw new RuntimeException("ML 내용증명서 생성 중 오류가 발생했습니다: " + e.getMessage());
//        }
//    }
//
//    /**
//     * 특약사항 생성 요청 (실제 ML API 엔드포인트: /api/v1/contract/generate-special-terms)
//     */
//    public Map<String, Object> generateSpecialTerms(Map<String, Object> contractData) {
//        log.info("⚖️ ML API 특약사항 생성 요청 시작 - Contract ID: {}", contractData.get("contract_id"));
//
//        try {
//            String url = mlApiBaseUrl + "/api/v1/contract/generate-special-terms";
//
//            HttpHeaders headers = new HttpHeaders();
//            headers.setContentType(MediaType.APPLICATION_JSON);
//            headers.set("User-Agent", "SuperLawva-Backend/1.0");
//
//            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(contractData, headers);
//
//            long startTime = System.currentTimeMillis();
//            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);
//
//            double processingTime = (System.currentTimeMillis() - startTime) / 1000.0;
//            log.info("✅ ML API 특약사항 생성 완료 - 처리시간: {}초", processingTime);
//
//            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
//                return response.getBody();
//            } else {
//                throw new RuntimeException("ML API 응답 오류: " + response.getStatusCode());
//            }
//
//        } catch (Exception e) {
//            log.error("❌ ML API 특약사항 생성 실패", e);
//            throw new RuntimeException("ML 특약사항 생성 중 오류가 발생했습니다: " + e.getMessage());
//        }
//    }
//
//    /**
//     * ML API 서버 상태 확인
//     */
//    public boolean isHealthy() {
//        try {
//            String url = mlApiBaseUrl + "/docs";
//            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
//            return response.getStatusCode() == HttpStatus.OK;
//        } catch (Exception e) {
//            log.error("ML API 서버 상태 확인 실패", e);
//            return false;
//        }
//    }
//}