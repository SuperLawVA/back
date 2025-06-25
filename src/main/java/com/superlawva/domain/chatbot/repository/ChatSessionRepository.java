package com.superlawva.domain.chatbot.repository;

import com.superlawva.domain.chatbot.entity.ChatSessionEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ChatSessionRepository extends JpaRepository<ChatSessionEntity, String> {
    
    // 사용자별 세션 조회 (최신순)
    Page<ChatSessionEntity> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    
    // 사용자별 세션 조회 (마지막 활동 시간 순)
    List<ChatSessionEntity> findByUserIdOrderByLastActiveAtDesc(Long userId);
    
    // 사용자의 활성 세션 조회
    List<ChatSessionEntity> findByUserIdAndStatus(Long userId, ChatSessionEntity.SessionStatus status);
    
    // 사용자의 최근 세션 조회
    Optional<ChatSessionEntity> findFirstByUserIdOrderByLastActiveAtDesc(Long userId);
    
    // 만료된 세션 조회 (정리용)
    @Query("SELECT s FROM ChatSessionEntity s WHERE s.lastActiveAt < :expiredTime AND s.status = 'active'")
    List<ChatSessionEntity> findExpiredSessions(@Param("expiredTime") LocalDateTime expiredTime);
    
    // 사용자별 총 세션 수
    Long countByUserId(Long userId);
    
    // 상태별 세션 수 (전체 시스템)
    Integer countByStatus(ChatSessionEntity.SessionStatus status);
} 