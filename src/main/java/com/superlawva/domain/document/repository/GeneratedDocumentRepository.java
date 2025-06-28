package com.superlawva.domain.document.repository;

import com.superlawva.domain.document.entity.GeneratedDocumentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface GeneratedDocumentRepository extends JpaRepository<GeneratedDocumentEntity, Long> {
    
    // 사용자별 생성 문서 조회 (최신순)
    List<GeneratedDocumentEntity> findByUserIdOrderByCreatedAtDesc(Long userId);
    
    // 생성 타입별 조회
    List<GeneratedDocumentEntity> findByUserIdAndGenerationTypeOrderByCreatedAtDesc(
            Long userId, GeneratedDocumentEntity.DocGenerationType generationType);
    
    // 상태별 조회
    List<GeneratedDocumentEntity> findByUserIdAndStatusOrderByCreatedAtDesc(
            Long userId, GeneratedDocumentEntity.DocumentStatus status);
    
    // 기간별 조회
    List<GeneratedDocumentEntity> findByUserIdAndCreatedAtBetweenOrderByCreatedAtDesc(
            Long userId, LocalDateTime startDate, LocalDateTime endDate);
    
    // Document ID로 GeneratedDocument 조회
    Optional<GeneratedDocumentEntity> findByDocumentId(Long documentId);
    
    // 평점 통계 조회
    @Query("SELECT AVG(g.userRating) FROM GeneratedDocumentEntity g WHERE g.userId = :userId AND g.userRating IS NOT NULL")
    Double findAverageRatingByUserId(@Param("userId") Long userId);
    
    // 생성 타입별 통계
    @Query("SELECT g.generationType, COUNT(g) FROM GeneratedDocumentEntity g WHERE g.userId = :userId GROUP BY g.generationType")
    List<Object[]> findGenerationTypeStatsByUserId(@Param("userId") Long userId);
    
    // 성능 통계 (평균 생성 시간)
    @Query("SELECT AVG(g.generationTimeSeconds) FROM GeneratedDocumentEntity g WHERE g.generationType = :type AND g.generationTimeSeconds IS NOT NULL")
    Double findAverageGenerationTimeByType(@Param("type") GeneratedDocumentEntity.DocGenerationType type);
    
    // 품질 점수별 조회
    List<GeneratedDocumentEntity> findByUserIdAndQualityScoreGreaterThanEqualOrderByQualityScoreDesc(
            Long userId, Integer minScore);
    
    // 모델별 통계
    List<GeneratedDocumentEntity> findByModelName(String modelName);
    
    // 통계 쿼리들
    long countByUserId(Long userId);
    
    long countByUserIdAndStatus(Long userId, GeneratedDocumentEntity.DocumentStatus status);
    
    // 최근 생성된 문서들
    List<GeneratedDocumentEntity> findTop10ByUserIdOrderByCreatedAtDesc(Long userId);
    
    // 높은 평점 문서들
    List<GeneratedDocumentEntity> findByUserIdAndUserRatingGreaterThanEqualOrderByUserRatingDescCreatedAtDesc(
            Long userId, Integer minRating);
} 