package com.superlawva.domain.chatbot.service;

import com.superlawva.domain.chatbot.dto.ChatbotRequestDTO;
import com.superlawva.domain.chatbot.dto.ChatbotResponseDTO;
import com.superlawva.domain.chatbot.dto.SessionListResponseDTO;
import com.superlawva.domain.chatbot.dto.ChatSessionResponseDTO;
import com.superlawva.domain.chatbot.entity.ChatSessionEntity;
import com.superlawva.domain.chatbot.entity.ChatMessageEntity;
import com.superlawva.domain.chatbot.repository.ChatSessionRepository;
import com.superlawva.domain.chatbot.repository.ChatMessageRepository;
import com.superlawva.domain.user.entity.User;
import com.superlawva.domain.user.repository.UserRepository;
import com.superlawva.global.exception.BaseException;
import com.superlawva.global.response.status.ErrorStatus;
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
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ChatbotService {
    
    private final ChatSessionRepository chatSessionRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatbotApiService chatbotApiService;
    private final UserRepository userRepository;
    
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
        
        // ML API에서 토큰 사용량 정보가 있으면 저장
        if (apiResponse.tokenUsage() != null) {
            botMessage = ChatMessageEntity.builder()
                    .session(session)
                    .role(ChatMessageEntity.MessageRole.assistant)
                    .content(botAnswer)
                    .questionType(apiResponse.questionType())
                    .responseTimeSeconds(apiResponse.responseTimeSeconds() != null ? 
                        BigDecimal.valueOf(apiResponse.responseTimeSeconds()) : null)
                    .tokenUsage(apiResponse.tokenUsage())
                    .build();
        }
        
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
     * 사용자별 세션 목록 조회 (기존)
     */
    @Transactional(readOnly = true)
    public Page<ChatSessionEntity> getUserSessions(User user, Pageable pageable) {
        log.info("사용자 {}의 세션 목록 조회", user.getId());
        return chatSessionRepository.findByUserIdOrderByCreatedAtDesc(user.getId(), pageable);
    }
    
    /**
     * 사용자별 세션 목록 조회 (간소화된 응답)
     */
    @Transactional(readOnly = true)
    public List<SessionListResponseDTO> getUserSessionList(Long userId) {
        log.info("사용자 {}의 간소화된 세션 목록 조회", userId);
        
        List<ChatSessionEntity> sessions = chatSessionRepository.findByUserIdOrderByLastActiveAtDesc(userId);
        
        return sessions.stream()
                .map(this::convertToSessionListResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * ChatSessionEntity를 SessionListResponseDTO로 변환
     */
    private SessionListResponseDTO convertToSessionListResponse(ChatSessionEntity session) {
        // 세션 제목 생성 (첫 번째 사용자 메시지 기반)
        String title = generateSessionTitle(session);
        
        return new SessionListResponseDTO(
                session.getSessionId(),
                title,
                session.getLastActiveAt()
        );
    }
    
    /**
     * 세션 제목 생성
     */
    private String generateSessionTitle(ChatSessionEntity session) {
        try {
            // 첫 번째 사용자 메시지를 세션 제목으로 사용
            Optional<ChatMessageEntity> firstUserMessage = chatMessageRepository
                    .findFirstBySessionSessionIdAndRoleOrderByCreatedAtAsc(
                            session.getSessionId(), 
                            ChatMessageEntity.MessageRole.user
                    );
            
            if (firstUserMessage.isPresent()) {
                String content = firstUserMessage.get().getContent();
                // 제목이 너무 길면 축약
                if (content.length() > 30) {
                    return content.substring(0, 30) + "...";
                }
                return content;
            }
            
            // 첫 번째 메시지가 없으면 질문 유형 기반으로 제목 생성
            if (session.getLastQuestionType() != null) {
                return switch (session.getLastQuestionType()) {
                    case "legal_rag" -> "법률 상담";
                    case "case_rag" -> "판례 상담";
                    case "first_chat" -> "새로운 상담";
                    default -> "챗봇 상담";
                };
            }
            
            return "새로운 대화";
            
        } catch (Exception e) {
            log.warn("세션 {} 제목 생성 중 오류: {}", session.getSessionId(), e.getMessage());
            return "챗봇 상담";
        }
    }
    
    /**
     * 새 세션 생성
     */
    @Transactional
    public ChatSessionResponseDTO createSession(User user) {
        User sessionUser = user;
        if (sessionUser == null) {
            log.warn("인증 정보 없이 세션 생성을 시도합니다. 임시 사용자로 세션을 생성합니다.");
            // 임시 사용자 객체 생성 (실제 DB에는 존재하지 않음)
            sessionUser = User.builder().id(0L).nickname("Anonymous").build();
        }
        ChatSessionEntity newSession = new ChatSessionEntity(sessionUser);
        ChatSessionEntity savedSession = chatSessionRepository.save(newSession);
        return ChatSessionResponseDTO.fromEntity(savedSession);
    }

    /**
     * 세션 완전 삭제 (ML 팀 스펙)
     * 세션과 관련된 모든 메시지를 함께 삭제
     */
    public boolean deleteSession(String sessionId, User user) {
        Optional<ChatSessionEntity> session = chatSessionRepository.findById(sessionId);
        
        if (session.isEmpty()) {
            log.warn("존재하지 않는 세션 {} 삭제 시도 - 사용자: {}", sessionId, user.getId());
            return false;
        }
        
        if (!session.get().getUser().getId().equals(user.getId())) {
            log.warn("사용자 {}가 권한 없는 세션 {} 삭제 시도", user.getId(), sessionId);
            return false;
        }
        
        try {
            // 1. 해당 세션의 모든 메시지 삭제
            chatMessageRepository.deleteBySessionSessionId(sessionId);
            log.info("세션 {}의 모든 메시지 삭제 완료", sessionId);
            
            // 2. 세션 삭제
            chatSessionRepository.delete(session.get());
            log.info("세션 {} 완전 삭제 완료 - 사용자: {}", sessionId, user.getId());
            
            return true;
        } catch (Exception e) {
            log.error("세션 {} 삭제 중 오류 발생: {}", sessionId, e.getMessage(), e);
            return false;
        }
    }


} 