package com.superlawva.domain.search.service;


import com.superlawva.domain.search.dto.SearchRequestDTO;
import com.superlawva.domain.search.dto.SearchResponseDTO;
import com.superlawva.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchService {
    
    private final RestTemplate restTemplate;
    
    @Value("${chatbot.api.base-url}")
    private String mlApiBaseUrl;
    
    /**
     * ML 팀 검색 API 호출
     */
    public SearchResponseDTO searchDocuments(SearchRequestDTO request, User user) {
        log.info("사용자 {}의 문서 검색 요청: {}", user.getId(), request.query());
        
        try {
            // ML 팀 API 호출
            String url = mlApiBaseUrl + "/api/v1/search";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            // 요청 바디 구성
            Map<String, Object> requestBody = Map.of(
                    "query", request.query(),
                    "search_type", request.search_type(),
                    "k", request.k()
            );
            
            HttpEntity<Map<String, Object>> httpEntity = new HttpEntity<>(requestBody, headers);
            
            long startTime = System.currentTimeMillis();
            ResponseEntity<SearchResponseDTO> response = restTemplate.postForEntity(
                    url, 
                    httpEntity, 
                    SearchResponseDTO.class
            );
            long endTime = System.currentTimeMillis();
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                SearchResponseDTO result = response.getBody();
                double searchTimeSeconds = (endTime - startTime) / 1000.0;
                
                log.info("검색 성공 - 질의: '{}', 결과: {}개, 처리시간: {}ms", 
                         request.query(), result.totalResults(), (endTime - startTime));
                
                // 검색 성공 (검색 결과는 ChatbotService에서 저장)
                
                return result;
            } else {
                log.error("ML 팀 검색 API 호출 실패 - 상태코드: {}", response.getStatusCode());
                
                // 검색 실패
                
                return createErrorResponse("검색 서비스에 일시적인 문제가 발생했습니다.");
            }
            
        } catch (Exception e) {
            log.error("ML 팀 검색 API 호출 중 오류 발생: {}", e.getMessage(), e);
            
            // 검색 예외 발생
            
            return createErrorResponse("검색 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
        }
    }
    
    /**
     * 오류 응답 생성
     */
    private SearchResponseDTO createErrorResponse(String errorMessage) {
        return new SearchResponseDTO(
                java.util.List.of(
                        new SearchResponseDTO.DocumentResult(
                                "오류 발생",
                                errorMessage,
                                0.0,
                                new SearchResponseDTO.DocumentMetadata("error", "system", "", "")
                        )
                ),
                0.0,
                0
        );
    }
} 