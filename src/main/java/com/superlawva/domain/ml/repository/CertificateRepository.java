package com.superlawva.domain.ml.repository;

import com.superlawva.domain.ml.entity.CertificateEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CertificateRepository extends JpaRepository<CertificateEntity, Long> {

    // Contract ID로 내용증명서 조회
    List<CertificateEntity> findByContractId(String contractId);

    // User ID로 내용증명서 조회
    List<CertificateEntity> findByUserId(String userId);

    // Contract ID와 User ID로 내용증명서 조회 (보안)
    List<CertificateEntity> findByContractIdAndUserId(String contractId, String userId);

    // 특정 사용자의 특정 상태 내용증명서 조회
    List<CertificateEntity> findByUserIdAndStatus(String userId, String status);

    // Contract ID로 최신 내용증명서 조회
    Optional<CertificateEntity> findTopByContractIdOrderByCreatedDateDesc(String contractId);

    // 상태별 조회
    List<CertificateEntity> findByStatus(String status);

    // 날짜 범위로 조회
    List<CertificateEntity> findByCreatedDateBetween(LocalDateTime startDate, LocalDateTime endDate);

    // 성공한 내용증명서만 조회
    List<CertificateEntity> findByStatusAndContractId(String status, String contractId);

    // 사용자 ID와 날짜 범위로 조회
    List<CertificateEntity> findByUserIdAndCreatedDateBetween(String userId, LocalDateTime startDate, LocalDateTime endDate);
}