package com.superlawva.domain.ml.repository;

import com.superlawva.domain.ml.entity.Certificate;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CertificateRepository extends MongoRepository<Certificate, String> {
    
    // Contract ID로 내용증명서 조회
    List<Certificate> findByContractId(String contractId);
    
    // User ID로 내용증명서 조회
    List<Certificate> findByUserId(String userId);
    
    // Contract ID와 User ID로 내용증명서 조회 (보안)
    List<Certificate> findByContractIdAndUserId(String contractId, String userId);
    
    // 특정 사용자의 특정 상태 내용증명서 조회
    List<Certificate> findByUserIdAndStatus(String userId, String status);
    
    // Contract ID로 최신 내용증명서 조회
    Optional<Certificate> findTopByContractIdOrderByCreatedDateDesc(String contractId);
    
    // 상태별 조회
    List<Certificate> findByStatus(String status);
    
    // 날짜 범위로 조회
    List<Certificate> findByCreatedDateBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    // ML Certificate ID로 조회
    Optional<Certificate> findByMlCertificateId(Integer mlCertificateId);
    
    // 성공한 내용증명서만 조회
    List<Certificate> findByStatusAndContractId(String status, String contractId);
    
    // 사용자 ID와 날짜 범위로 조회
    List<Certificate> findByUserIdAndCreatedDateBetween(String userId, LocalDateTime startDate, LocalDateTime endDate);
} 