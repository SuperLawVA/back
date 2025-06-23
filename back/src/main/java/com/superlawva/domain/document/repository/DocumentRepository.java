package com.superlawva.domain.document.repository;

import com.superlawva.domain.document.entity.Document;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentRepository extends MongoRepository<Document, String> {
    
    // 사용자별 문서 조회 (최신순)
    List<Document> findByUserIdOrderByCreatedAtDesc(Long userId);
    
    // 문서 유형별 조회
    List<Document> findByUserIdAndDocumentTypeOrderByCreatedAtDesc(Long userId, Document.DocumentType documentType);
    
    // 상태별 조회
    List<Document> findByUserIdAndStatusOrderByCreatedAtDesc(Long userId, Document.DocumentStatus status);
    
    // 파일명으로 검색
    List<Document> findByUserIdAndOriginalFilenameContainingIgnoreCaseOrderByCreatedAtDesc(Long userId, String filename);
    
    // 기간별 조회
    List<Document> findByUserIdAndCreatedAtBetweenOrderByCreatedAtDesc(Long userId, LocalDateTime startDate, LocalDateTime endDate);
    
    // GridFS 파일 ID로 조회
    Optional<Document> findByGridfsFileId(String gridfsFileId);
    
    // 저장 방식별 조회
    List<Document> findByUserIdAndStorageTypeOrderByCreatedAtDesc(Long userId, Document.StorageType storageType);
    
    // 파일 크기 기준 조회
    @Query("{'userId': ?0, 'fileSizeBytes': {'$gte': ?1, '$lte': ?2}}")
    List<Document> findByUserIdAndFileSizeBetween(Long userId, Long minSize, Long maxSize);
    
    // 메타데이터 검색 (MongoDB의 JSON 쿼리 활용)
    @Query("{'userId': ?0, 'metadata.?1': ?2}")
    List<Document> findByUserIdAndMetadataField(Long userId, String fieldName, Object value);
    
    // 통계 쿼리들
    @Query(value = "{'userId': ?0}", count = true)
    long countByUserId(Long userId);
    
    @Query(value = "{'userId': ?0, 'status': ?1}", count = true)
    long countByUserIdAndStatus(Long userId, Document.DocumentStatus status);
} 