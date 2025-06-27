package com.superlawva.domain.chatbot.repository;

import com.superlawva.domain.chatbot.entity.ChatMessageEntity;
import com.superlawva.domain.chatbot.entity.ChatSessionEntity;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessageEntity, Long> {
    
    // 세션별 메시지 조회 (시간순)
    List<ChatMessageEntity> findBySessionSessionIdOrderByCreatedAtAsc(String sessionId);
    
    // 사용자별 메시지 조회 (최신순)
    @Query("SELECT m FROM ChatMessageEntity m WHERE m.session.user.id = :userId ORDER BY m.createdAt DESC")
    Page<ChatMessageEntity> findByUserIdOrderByCreatedAtDesc(@Param("userId") Long userId, Pageable pageable);
    
    // 세션의 마지막 메시지 조회
    ChatMessageEntity findFirstBySessionSessionIdOrderByCreatedAtDesc(String sessionId);
    
    // 사용자의 최근 대화 조회
    @Query("SELECT m FROM ChatMessageEntity m WHERE m.session.user.id = :userId AND m.createdAt >= :since ORDER BY m.createdAt DESC")
    List<ChatMessageEntity> findRecentMessagesByUser(@Param("userId") Long userId, @Param("since") LocalDateTime since);
    
    // 질문 유형별 통계
    @Query("SELECT m.questionType, COUNT(m) FROM ChatMessageEntity m WHERE m.role = 'assistant' AND m.questionType IS NOT NULL GROUP BY m.questionType")
    List<Object[]> getQuestionTypeStatistics();
    
    // 응답 시간 통계
    @Query("SELECT AVG(m.responseTimeSeconds) FROM ChatMessageEntity m WHERE m.role = 'assistant' AND m.responseTimeSeconds IS NOT NULL")
    Double getAverageResponseTime();
    
    // 세션별 메시지 삭제 (ML 팀 스펙용)
    void deleteBySessionSessionId(String sessionId);
    
    // 세션의 첫 번째 사용자 메시지 조회 (제목 생성용)
    Optional<ChatMessageEntity> findFirstBySessionSessionIdAndRoleOrderByCreatedAtAsc(String sessionId, ChatMessageEntity.MessageRole role);

    void deleteBySession(ChatSessionEntity session);
} 