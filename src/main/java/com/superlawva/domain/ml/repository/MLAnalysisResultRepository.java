package com.superlawva.domain.ml.repository;

import com.superlawva.domain.ml.entity.MLAnalysisResult;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface MLAnalysisResultRepository extends MongoRepository<MLAnalysisResult, String> {
    
    // Contract ID로 분석 결과 조회
    List<MLAnalysisResult> findByContractId(String contractId);
    
    // User ID로 분석 결과 조회
    List<MLAnalysisResult> findByUserId(String userId);
    
    // Contract ID와 User ID로 분석 결과 조회 (보안)
    List<MLAnalysisResult> findByContractIdAndUserId(String contractId, String userId);
    
    // 분석 타입별 조회
    List<MLAnalysisResult> findByAnalysisType(String analysisType);
    
    // 특정 사용자의 특정 타입 분석 결과 조회
    List<MLAnalysisResult> findByUserIdAndAnalysisType(String userId, String analysisType);
    
    // Contract ID와 분석 타입으로 최신 결과 조회
    Optional<MLAnalysisResult> findTopByContractIdAndAnalysisTypeOrderByCreatedDateDesc(
            String contractId, String analysisType);
    
    // 상태별 조회
    List<MLAnalysisResult> findByStatus(String status);
    
    // 날짜 범위로 조회
    List<MLAnalysisResult> findByCreatedDateBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    // ML Analysis ID로 조회
    Optional<MLAnalysisResult> findByMlAnalysisId(Integer mlAnalysisId);
    
    // 성공한 분석 결과만 조회
    List<MLAnalysisResult> findByStatusAndContractId(String status, String contractId);
} 