package com.superlawva.domain.log.repository;

import com.superlawva.domain.log.entity.SearchResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SearchResultRepository extends JpaRepository<SearchResult, Long> {
    
    /**
     * 특정 메시지의 검색 결과 조회
     * search_logs.msg_id = chatbot_messages.msg_id 관계
     */
    List<SearchResult> findByMessageIdOrderByCreatedAtAsc(Long messageId);
    
    /**
     * 특정 세션의 모든 검색 결과 조회
     */
    @Query("SELECT sr FROM SearchResult sr WHERE sr.message.session.sessionId = :sessionId ORDER BY sr.createdAt ASC")
    List<SearchResult> findBySessionId(@Param("sessionId") String sessionId);
    
    /**
     * 특정 사용자의 모든 검색 결과 조회 (최신순)
     */
    @Query("SELECT sr FROM SearchResult sr WHERE sr.message.session.user.id = :userId ORDER BY sr.createdAt DESC")
    List<SearchResult> findByUserIdOrderByCreatedAtDesc(@Param("userId") Long userId);
    
    /**
     * 문서 ID로 검색 결과 조회
     */
    List<SearchResult> findByDocId(String docId);
    
    /**
     * 유사도 점수 기준으로 상위 결과 조회
     */
    @Query("SELECT sr FROM SearchResult sr WHERE sr.similarityScore >= :minScore ORDER BY sr.similarityScore DESC")
    List<SearchResult> findByMinSimilarityScore(@Param("minScore") java.math.BigDecimal minScore);
} 