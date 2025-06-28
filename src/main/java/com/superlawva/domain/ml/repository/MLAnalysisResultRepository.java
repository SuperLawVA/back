package com.superlawva.domain.ml.repository;

import com.superlawva.domain.ml.entity.MLAnalysisResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface MLAnalysisResultRepository extends JpaRepository<MLAnalysisResult, Long> {

    // Contract ID로 분석 결과 조회
    List<MLAnalysisResult> findByContractId(String contractId);

    // User ID로 분석 결과 조회
    List<MLAnalysisResult> findByUserId(String userId);

    // Contract ID와 User ID로 분석 결과 조회 (보안)
    List<MLAnalysisResult> findByContractIdAndUserId(String contractId, String userId);

    // 특정 사용자의 특정 타입 분석 결과 조회
    List<MLAnalysisResult> findByUserIdAndAnalysisType(String userId, String analysisType);

    // 상태별 조회
    List<MLAnalysisResult> findByStatus(String status);

    // 날짜 범위로 조회
    List<MLAnalysisResult> findByCreatedDateBetween(LocalDateTime startDate, LocalDateTime endDate);

    // ML Response ID로 조회
    Optional<MLAnalysisResult> findByRawMlResponseId(Integer rawMlResponseId);

    // 성공한 분석 결과만 조회
    List<MLAnalysisResult> findByStatusAndContractId(String status, String contractId);

    // 사용자별 최근 분석 결과 조회 (최신순)
    List<MLAnalysisResult> findByUserIdOrderByCreatedDateDesc(String userId);

    // 특정 위험도 이상의 분석 결과 조회
    @Query("SELECT m FROM MLAnalysisResult m WHERE m.riskScore >= :minScore")
    List<MLAnalysisResult> findByRiskScoreGreaterThanEqual(@Param("minScore") Double minScore);

    // 특정 위험 레벨의 분석 결과 조회
    List<MLAnalysisResult> findByRiskLevel(String riskLevel);

    // 사용자의 최근 N개 분석 결과 조회
    @Query("SELECT m FROM MLAnalysisResult m WHERE m.userId = :userId ORDER BY m.createdDate DESC LIMIT :limit")
    List<MLAnalysisResult> findRecentByUserId(@Param("userId") String userId, @Param("limit") int limit);
}
