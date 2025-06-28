package com.superlawva.domain.document.repository;

import com.superlawva.domain.document.entity.DocumentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentRepository extends JpaRepository<DocumentEntity, Long> {
    
    // 사용자별 문서 조회 (최신순)
    List<DocumentEntity> findByUserIdOrderByCreatedAtDesc(Long userId);
    
    // 문서 유형별 조회
    List<DocumentEntity> findByUserIdAndDocumentTypeOrderByCreatedAtDesc(Long userId, DocumentEntity.DocumentType documentType);
    
    // 상태별 조회
    List<DocumentEntity> findByUserIdAndStatusOrderByCreatedAtDesc(Long userId, DocumentEntity.DocumentStatus status);
    
    // 파일명으로 검색
    List<DocumentEntity> findByUserIdAndOriginalFilenameContainingIgnoreCaseOrderByCreatedAtDesc(Long userId, String filename);
    
    // 기간별 조회
    List<DocumentEntity> findByUserIdAndCreatedAtBetweenOrderByCreatedAtDesc(Long userId, LocalDateTime startDate, LocalDateTime endDate);
    
    // GridFS 파일 ID로 조회 (향후 삭제 예정)
    Optional<DocumentEntity> findByGridfsFileId(String gridfsFileId);
    
    // 저장 방식별 조회
    List<DocumentEntity> findByUserIdAndStorageTypeOrderByCreatedAtDesc(Long userId, DocumentEntity.StorageType storageType);
    
    // 파일 크기 기준 조회
    @Query("SELECT d FROM DocumentEntity d WHERE d.userId = :userId AND d.fileSizeBytes BETWEEN :minSize AND :maxSize")
    List<DocumentEntity> findByUserIdAndFileSizeBetween(@Param("userId") Long userId, @Param("minSize") Long minSize, @Param("maxSize") Long maxSize);
    
    // 통계 쿼리들
    long countByUserId(Long userId);
    
    long countByUserIdAndStatus(Long userId, DocumentEntity.DocumentStatus status);
} 