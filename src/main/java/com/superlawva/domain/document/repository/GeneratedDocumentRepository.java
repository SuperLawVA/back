package com.superlawva.domain.document.repository;

import com.superlawva.domain.document.entity.GeneratedDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface GeneratedDocumentRepository extends MongoRepository<GeneratedDocument, String> {
    
    // 사용자별 생성 문서 조회 (최신순)
    List<GeneratedDocument> findByUserIdOrderByCreatedAtDesc(Long userId);
    
    // 생성 타입별 조회
    List<GeneratedDocument> findByUserIdAndGenerationTypeOrderByCreatedAtDesc(
            Long userId, GeneratedDocument.GenerationType generationType);
    
    // 상태별 조회
    List<GeneratedDocument> findByUserIdAndStatusOrderByCreatedAtDesc(
            Long userId, GeneratedDocument.DocumentStatus status);
    
    // 기간별 조회
    List<GeneratedDocument> findByUserIdAndCreatedAtBetweenOrderByCreatedAtDesc(
            Long userId, LocalDateTime startDate, LocalDateTime endDate);
    
    // Document ID로 GeneratedDocument 조회 (MongoDB String ID 사용)
    Optional<GeneratedDocument> findByDocumentId(String documentId);
    
    // 평점 통계 조회 (MongoDB Aggregation)
    @Aggregation(pipeline = {
        "{ '$match': { 'userId': ?0, 'userRating': { '$ne': null } } }",
        "{ '$group': { '_id': null, 'avgRating': { '$avg': '$userRating' } } }"
    })
    Double findAverageRatingByUserId(Long userId);
    
    // 생성 타입별 통계 (MongoDB Aggregation)
    @Aggregation(pipeline = {
        "{ '$match': { 'userId': ?0 } }",
        "{ '$group': { '_id': '$generationType', 'count': { '$sum': 1 } } }"
    })
    List<Object> findGenerationTypeStatsByUserId(Long userId);
    
    // 성능 통계 (평균 생성 시간)
    @Aggregation(pipeline = {
        "{ '$match': { 'generationType': ?0, 'generationTimeSeconds': { '$ne': null } } }",
        "{ '$group': { '_id': null, 'avgTime': { '$avg': '$generationTimeSeconds' } } }"
    })
    Double findAverageGenerationTimeByType(GeneratedDocument.GenerationType type);
    
    // 품질 점수별 조회
    List<GeneratedDocument> findByUserIdAndQualityScoreGreaterThanEqualOrderByQualityScoreDesc(
            Long userId, Integer minScore);
    
    // 모델별 통계
    @Query("{'modelName': ?0}")
    List<GeneratedDocument> findByModelName(String modelName);
    
    // 고급 검색 - 메타데이터 기반
    @Query("{'userId': ?0, 'additionalMetadata.?1': ?2}")
    List<GeneratedDocument> findByUserIdAndMetadataField(Long userId, String fieldName, Object value);
    
    // 통계 쿼리들
    @Query(value = "{'userId': ?0}", count = true)
    long countByUserId(Long userId);
    
    @Query(value = "{'userId': ?0, 'status': ?1}", count = true)
    long countByUserIdAndStatus(Long userId, GeneratedDocument.DocumentStatus status);
    
    // 최근 생성된 문서들
    List<GeneratedDocument> findTop10ByUserIdOrderByCreatedAtDesc(Long userId);
    
    // 높은 평점 문서들
    List<GeneratedDocument> findByUserIdAndUserRatingGreaterThanEqualOrderByUserRatingDescCreatedAtDesc(
            Long userId, Integer minRating);
} 