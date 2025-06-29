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
        log.info("사용자 {}의 챗봇 메시지 전송: {}", user != null ? user.getId() : "Anonymous", request.message());
        
        ChatSessionEntity session = getOrCreateSession(request.session_id(), user);
        
        ChatMessageEntity userMessage = ChatMessageEntity.createUserMessage(session, request.message());
        chatMessageRepository.save(userMessage);
        
        ChatbotApiService.ChatbotApiResult apiResult = chatbotApiService.sendMessage(
                request.message(),
                session.getSessionId(),
                user != null ? user.getId() : null
        );
        
        String botAnswer = apiResult.success() ? apiResult.answer() :
                          "죄송합니다. 일시적인 오류가 발생했습니다. 잠시 후 다시 시도해주세요.";
        
        ChatMessageEntity botMessage = ChatMessageEntity.createAssistantMessage(
                session,
                botAnswer,
                apiResult.questionType(),
                apiResult.responseTimeSeconds() != null ?
                    BigDecimal.valueOf(apiResult.responseTimeSeconds()) : null
        );
        
        chatMessageRepository.save(botMessage);
        
        session.updateActivity(apiResult.questionType());
        chatSessionRepository.save(session);
        
        String finalSessionId = apiResult.sessionId() != null ?
                               apiResult.sessionId() : session.getSessionId();
        
        if (apiResult.success()) {
            log.info("챗봇 응답 성공 - 세션: {}, 처리시간: {}초", finalSessionId, apiResult.responseTimeSeconds());
        } else {
            log.error("챗봇 API 호출 실패 - 세션: {}, 오류: {}", finalSessionId, apiResult.error());
        }
        
        return new ChatbotResponseDTO(
                botAnswer,
                finalSessionId,
                apiResult.questionType(),
                apiResult.responseTimeSeconds(),
                botMessage
        );
    }
    
    /**
     * 세션 조회 또는 생성
     */
    private ChatSessionEntity getOrCreateSession(String sessionId, User user) {
        if (sessionId != null && !sessionId.isBlank()) {
            return chatSessionRepository.findById(sessionId)
                    .orElseGet(() -> createNewSession(sessionId, user));
        }
        return createNewSession(null, user);
    }

    private ChatSessionEntity createNewSession(String sessionId, User user) {
        String newSessionId = (sessionId != null) ? sessionId : UUID.randomUUID().toString();
        
        ChatSessionEntity.ChatSessionEntityBuilder builder = ChatSessionEntity.builder()
                .sessionId(newSessionId)
                .status(ChatSessionEntity.SessionStatus.active);

        if (user != null) {
            builder.user(user);
        }

        return chatSessionRepository.save(builder.build());
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
        Optional<ChatSessionEntity> sessionOpt = chatSessionRepository.findById(sessionId);
        if (sessionOpt.isEmpty()) {
            throw new BaseException(ErrorStatus.CHAT_SESSION_NOT_FOUND);
        }
        ChatSessionEntity session = sessionOpt.get();
        
        // 익명 세션이 아니고, 세션의 소유자가 현재 사용자와 다를 경우 접근 제한
        if (session.getUser() != null && !session.getUser().getId().equals(user.getId())) {
            log.warn("사용자 {}가 권한 없는 세션 {}에 접근 시도", user.getId(), sessionId);
            throw new BaseException(ErrorStatus.FORBIDDEN_ACCESS_TO_SESSION);
        }
        
        return chatMessageRepository.findBySessionSessionIdOrderByCreatedAtAsc(sessionId);
    }
    
    /**
     * 사용자별 세션 목록 조회 (페이징)
     */
    @Transactional(readOnly = true)
    public Page<ChatSessionEntity> getUserSessions(User user, Pageable pageable) {
        log.info("사용자 {}의 세션 목록 조회", user.getId());
        return chatSessionRepository.findByUserIdOrderByCreatedAtDesc(user.getId(), pageable);
    }

    /**
     * 사용자별 세션 목록 조회 (페이징 없음)
     */
    @Transactional(readOnly = true)
    public List<ChatSessionEntity> getUserSessions(User user) {
        log.info("사용자 {}의 전체 세션 목록 조회", user.getId());
        return chatSessionRepository.findByUserIdOrderByLastActiveAtDesc(user.getId());
    }
    
    /**
     * 사용자별 세션 목록 조회 (간소화된 응답)
     * @deprecated 컨트롤러에서 직접 DTO로 변환하므로 더 이상 사용되지 않음
     */
    @Transactional(readOnly = true)
    @Deprecated
    public List<SessionListResponseDTO> getUserSessionList(Long userId) {
        if (userId == null) {
            log.warn("사용자 ID가 null이므로 빈 세션 목록을 반환합니다.");
            return List.of();
        }
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
    public ChatSessionEntity createSession(User user) {
        ChatSessionEntity.ChatSessionEntityBuilder builder = ChatSessionEntity.builder()
                .sessionId(UUID.randomUUID().toString())
                .status(ChatSessionEntity.SessionStatus.active);
        
        if (user != null) {
            log.info("사용자 {}를 위한 새 세션 생성", user.getId());
            builder.user(user);
        } else {
            log.info("익명 사용자를 위한 새 세션 생성");
        }
        
        ChatSessionEntity newSession = builder.build();
        return chatSessionRepository.save(newSession);
    }

    /**
     * 세션 삭제
     */
    @Transactional
    public boolean deleteSession(String sessionId, User user) {
        log.info("세션 {} 삭제 요청 (사용자: {})", sessionId, user != null ? user.getId() : "Anonymous");
        
        Optional<ChatSessionEntity> sessionOpt = chatSessionRepository.findById(sessionId);
        if (sessionOpt.isEmpty()) {
            log.warn("삭제하려는 세션 {}을 찾을 수 없습니다", sessionId);
            return false;
        }
        
        ChatSessionEntity session = sessionOpt.get();
        
        // 익명 세션이거나, 세션 소유자가 현재 사용자와 일치할 경우에만 삭제 허용
        if (session.getUser() == null || session.getUser().getId().equals(user.getId())) {
            try {
                // 1. 먼저 관련된 모든 메시지 삭제
                chatMessageRepository.deleteBySessionSessionId(sessionId);
                log.info("세션 {}의 모든 메시지 삭제 완료", sessionId);
                
                // 2. 세션 삭제
                chatSessionRepository.deleteById(sessionId);
                log.info("세션 {} 삭제 완료", sessionId);
                
                return true;
            } catch (Exception e) {
                log.error("세션 {} 삭제 중 오류 발생: {}", sessionId, e.getMessage(), e);
                throw new BaseException(ErrorStatus.INTERNAL_SERVER_ERROR);
            }
        } else {
            log.warn("사용자 {}가 권한 없는 세션 {} 삭제 시도", user.getId(), sessionId);
            throw new BaseException(ErrorStatus.FORBIDDEN);
        }
    }


} 