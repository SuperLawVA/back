package com.superlawva.domain.chatbot.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatbotApiService {
    
    private final RestTemplate restTemplate;
    
    @Value("${chatbot.api.base-url}")
    private String chatbotApiBaseUrl;
    
    @Value("${chatbot.api.timeout}")
    private int timeout;
    
    /**
     * ML 팀 챗봇 API 호출
     */
    public ChatbotApiResponse sendMessage(String message, String sessionId, Long userId) {
        try {
            String url = chatbotApiBaseUrl + "/api/v1/chat";
            
            // 세션 ID가 없으면 UUID 생성
            String finalSessionId = sessionId != null ? sessionId : generateUUID();
            
            // ML 팀 API 스펙에 맞는 요청 데이터
            Map<String, Object> requestBody = Map.of(
                    "message", message,
                    "session_id", finalSessionId
            );
            
            // HTTP 헤더 설정
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            
            log.info("ML 팀 챗봇 API 호출: {} - 세션: {}", url, finalSessionId);
            long startTime = System.currentTimeMillis();
            
            // API 호출
            ResponseEntity<Map> response = restTemplate.exchange(
                    url, 
                    HttpMethod.POST, 
                    request, 
                    Map.class
            );
            
            long processingTimeMs = System.currentTimeMillis() - startTime;
            double processingTimeSeconds = processingTimeMs / 1000.0;
            
            log.info("챗봇 API 응답 완료 - 처리시간: {}ms", processingTimeMs);
            
            // ML 팀 응답 처리
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();
                
                return new ChatbotApiResponse(
                        true,
                        (String) responseBody.get("answer"),
                        (String) responseBody.get("session_id"),
                        (String) responseBody.get("question_type"),
                        processingTimeSeconds,
                        (String) responseBody.get("token_usage"),
                        null
                );
            } else {
                return new ChatbotApiResponse(
                        false,
                        null,
                        finalSessionId,
                        null,
                        processingTimeSeconds,
                        null,
                        "ML API 응답이 비어있습니다."
                );
            }
            
        } catch (Exception e) {
            log.error("ML 팀 챗봇 API 호출 실패: ", e);
            return new ChatbotApiResponse(
                    false,
                    null,
                    sessionId,
                    null,
                    0.0,
                    null,
                    "API 호출 실패: " + e.getMessage()
            );
        }
    }
    
    /**
     * UUID 기반 세션 ID 생성
     */
    private String generateUUID() {
        return UUID.randomUUID().toString();
    }
    
    /**
     * ML 팀 API 응답 데이터
     */
    public record ChatbotApiResponse(
            boolean success,
            String answer,
            String sessionId,
            String questionType,
            Double responseTimeSeconds,
            String tokenUsage,
            String error
    ) {}
} 