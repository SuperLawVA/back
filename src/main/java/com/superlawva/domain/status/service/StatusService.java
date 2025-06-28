package com.superlawva.domain.status.service;

import com.superlawva.domain.chatbot.repository.ChatSessionRepository;
import com.superlawva.domain.status.dto.StatusResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class StatusService {
    
    private final RestTemplate restTemplate;
    private final ChatSessionRepository chatSessionRepository;
    
    @Value("${api.servers.chatbot.base-url:${chatbot.api.base-url}}")
    private String mlApiBaseUrl;
    
    /**
     * ML 팀 상태 API 호출 및 자체 상태 확인
     */
    public StatusResponseDTO getStatus() {
        try {
            // 1. 자체 시스템 상태 확인
            Integer activeSessions = getActiveSessionCount();
            log.info("현재 활성 세션 수: {}", activeSessions);
            
            // 2. ML 팀 상태 API 호출
            String url = mlApiBaseUrl + "/api/v1/status";
            
            long startTime = System.currentTimeMillis();
            ResponseEntity<StatusResponseDTO> response = restTemplate.getForEntity(url, StatusResponseDTO.class);
            long endTime = System.currentTimeMillis();
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                StatusResponseDTO mlStatus = response.getBody();
                log.info("ML 팀 상태 확인 성공 - 상태: {}, 응답시간: {}ms", 
                         mlStatus.status(), (endTime - startTime));
                
                // ML 팀 응답에 우리 시스템 정보 추가
                return new StatusResponseDTO(
                        mlStatus.status(),
                        activeSessions, // 우리 시스템의 활성 세션 수
                        mlStatus.models(),
                        mlStatus.vectorSearchReady(),
                        mlStatus.timestamp()
                );
            } else {
                log.warn("ML 팀 상태 API 호출 실패 - 상태코드: {}", response.getStatusCode());
                return StatusResponseDTO.degraded(activeSessions, "ML API 연결 실패");
            }
            
        } catch (Exception e) {
            log.error("상태 확인 중 오류 발생: {}", e.getMessage(), e);
            Integer activeSessions = getActiveSessionCount();
            return StatusResponseDTO.degraded(activeSessions, "시스템 오류");
        }
    }
    
    /**
     * 자체 시스템 활성 세션 수 조회
     */
    private Integer getActiveSessionCount() {
        try {
            return chatSessionRepository.countByStatus(
                com.superlawva.domain.chatbot.entity.ChatSessionEntity.SessionStatus.active
            );
        } catch (Exception e) {
            log.error("활성 세션 수 조회 실패: {}", e.getMessage());
            return 0;
        }
    }
    
    /**
     * ML 팀 API 연결 상태만 확인 (간단한 헬스체크)
     */
    public boolean isMLServiceHealthy() {
        try {
            String url = mlApiBaseUrl + "/api/v1/status";
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            log.warn("ML 서비스 헬스체크 실패: {}", e.getMessage());
            return false;
        }
    }
} 