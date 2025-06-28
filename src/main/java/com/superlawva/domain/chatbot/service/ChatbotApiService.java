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

import com.fasterxml.jackson.annotation.JsonProperty;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatbotApiService {
    
    private final RestTemplate restTemplate;
    
    @Value("${api.servers.chatbot.base-url:${chatbot.api.base-url}}")
    private String chatbotApiBaseUrl;
    
    @Value("${chatbot.api.timeout}")
    private int timeout;
    
    /**
     * ML 팀 챗봇 API 호출
     */
    public ChatbotApiResult sendMessage(String message, String sessionId, Long userId) {
        String url = chatbotApiBaseUrl + "/api/v1/chat";
        String finalSessionId = sessionId != null ? sessionId : UUID.randomUUID().toString();

        Map<String, Object> requestBody = Map.of(
                "message", message,
                "session_id", finalSessionId
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        try {
            log.info("ML 팀 챗봇 API 호출: {} - 세션: {}", url, finalSessionId);
            ResponseEntity<ChatbotApiResult> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    request,
                    ChatbotApiResult.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                log.info("챗봇 API 응답 성공 - 세션: {}", finalSessionId);
                return response.getBody();
            } else {
                log.error("ML 챗봇 API 응답 오류: 상태코드 {}", response.getStatusCode());
                return ChatbotApiResult.createErrorResult(finalSessionId, "ML API 응답이 비어있습니다.");
            }
        } catch (Exception e) {
            log.error("ML 팀 챗봇 API 호출 실패: ", e);
            return ChatbotApiResult.createErrorResult(finalSessionId, "API 호출 실패: " + e.getMessage());
        }
    }
    
    /**
     * UUID 기반 세션 ID 생성
     */
    private String generateUUID() {
        return UUID.randomUUID().toString();
    }
    
    /**
     * ML 팀 API 응답을 위한 내부 DTO
     */
    public record ChatbotApiResult(
        @JsonProperty("answer") String answer,
        @JsonProperty("session_id") String sessionId,
        @JsonProperty("question_type") String questionType,
        @JsonProperty("response_time_seconds") Double responseTimeSeconds,
        String error // 에러 발생 시에만 사용
    ) {
        public boolean success() {
            return error == null;
        }

        public static ChatbotApiResult createErrorResult(String sessionId, String errorMessage) {
            return new ChatbotApiResult(null, sessionId, null, 0.0, errorMessage);
        }
    }
} 