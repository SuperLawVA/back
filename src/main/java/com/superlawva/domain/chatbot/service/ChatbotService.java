package com.superlawva.domain.chatbot.service;

import com.superlawva.domain.chatbot.dto.ChatbotRequestDTO;
import com.superlawva.domain.chatbot.dto.ChatbotResponseDTO;
import com.superlawva.domain.chatbot.entity.ChatSessionEntity;
import com.superlawva.domain.chatbot.entity.ChatMessageEntity;
import com.superlawva.domain.chatbot.repository.ChatSessionRepository;
import com.superlawva.domain.chatbot.repository.ChatMessageRepository;
import com.superlawva.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ChatbotService {
    
    private final ChatSessionRepository chatSessionRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatbotApiService chatbotApiService;
    
    /**
     * 챗봇에게 메시지 전송 (ML 팀 API 연동)
     */
    public ChatbotResponseDTO sendMessage(ChatbotRequestDTO request, User user) {
        log.info("사용자 {}의 챗봇 메시지 전송: {}", user.getId(), request.message());
        
        // 1. 세션 조회 또는 생성
        ChatSessionEntity session = getOrCreateSession(request.session_id(), user);
        
        // 2. 사용자 메시지 저장
        ChatMessageEntity userMessage = ChatMessageEntity.createUserMessage(session, request.message());
        chatMessageRepository.save(userMessage);
        
        // 3. ML 팀 API 호출
        ChatbotApiService.ChatbotApiResponse apiResponse = chatbotApiService.sendMessage(
                request.message(),
                session.getSessionId(),
                user.getId()
        );
        
        // 4. 봇 응답 저장
        String botAnswer = apiResponse.success() ? apiResponse.answer() : 
                          "죄송합니다. 일시적인 오류가 발생했습니다. 잠시 후 다시 시도해주세요.";
        
        ChatMessageEntity botMessage = ChatMessageEntity.createAssistantMessage(
                session,
                botAnswer,
                apiResponse.questionType(),
                apiResponse.responseTimeSeconds() != null ? 
                    BigDecimal.valueOf(apiResponse.responseTimeSeconds()) : null
        );
        chatMessageRepository.save(botMessage);
        
        // 5. 세션 활동 업데이트
        session.updateActivity(apiResponse.questionType());
        chatSessionRepository.save(session);
        
        // 6. ML 팀 스펙 응답 반환
        String finalSessionId = apiResponse.sessionId() != null ? 
                               apiResponse.sessionId() : session.getSessionId();
        
        if (apiResponse.success()) {
            log.info("챗봇 응답 성공 - 세션: {}, 처리시간: {}초", finalSessionId, apiResponse.responseTimeSeconds());
        } else {
            log.error("챗봇 API 호출 실패 - 세션: {}, 오류: {}", finalSessionId, apiResponse.error());
        }
        
        return ChatbotResponseDTO.from(
                botAnswer,
                finalSessionId,
                apiResponse.questionType(),
                apiResponse.responseTimeSeconds()
        );
    }
    
    /**
     * 세션 조회 또는 생성
     */
    private ChatSessionEntity getOrCreateSession(String sessionId, User user) {
        if (sessionId != null) {
            Optional<ChatSessionEntity> existingSession = chatSessionRepository.findById(sessionId);
            if (existingSession.isPresent()) {
                return existingSession.get();
            }
        }
        
        // 새 세션 생성
        String newSessionId = sessionId != null ? sessionId : UUID.randomUUID().toString();
        ChatSessionEntity newSession = ChatSessionEntity.builder()
                .sessionId(newSessionId)
                .user(user)
                .status(ChatSessionEntity.SessionStatus.active)
                .build();
        
        return chatSessionRepository.save(newSession);
    }
    
    /**
     * 사용자별 대화 이력 조회 (페이징)
     */
    @Transactional(readOnly = true)
    public Page<ChatMessageEntity> getChatHistory(User user, Pageable pageable) {
        log.info("사용자 {}의 대화 이력 조회", user.getId());
        return chatMessageRepository.findByUserIdOrderByCreatedAtDesc(user.getId(), pageable);
    }
    
    /**
     * 세션별 대화 이력 조회
     */
    @Transactional(readOnly = true)
    public List<ChatMessageEntity> getSessionHistory(String sessionId, User user) {
        log.info("세션 {}의 대화 이력 조회", sessionId);
        
        // 보안: 세션이 해당 사용자의 것인지 확인
        Optional<ChatSessionEntity> session = chatSessionRepository.findById(sessionId);
        if (session.isEmpty() || !session.get().getUser().getId().equals(user.getId())) {
            log.warn("사용자 {}가 권한 없는 세션 {}에 접근 시도", user.getId(), sessionId);
            return List.of();
        }
        
        return chatMessageRepository.findBySessionSessionIdOrderByCreatedAtAsc(sessionId);
    }
    
    /**
     * 사용자별 세션 목록 조회
     */
    @Transactional(readOnly = true)
    public Page<ChatSessionEntity> getUserSessions(User user, Pageable pageable) {
        log.info("사용자 {}의 세션 목록 조회", user.getId());
        return chatSessionRepository.findByUserIdOrderByCreatedAtDesc(user.getId(), pageable);
    }
    
    /**
     * 세션 종료
     */
    public void closeSession(String sessionId, User user) {
        Optional<ChatSessionEntity> session = chatSessionRepository.findById(sessionId);
        if (session.isPresent() && session.get().getUser().getId().equals(user.getId())) {
            session.get().closeSession();
            chatSessionRepository.save(session.get());
            log.info("세션 {} 종료됨", sessionId);
        }
    }
} 