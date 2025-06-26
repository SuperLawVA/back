package com.superlawva.domain.ml.repository;

import com.superlawva.domain.ml.entity.MLAnalysisResult;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface MLAnalysisResultRepository extends MongoRepository<MLAnalysisResult, String> {
    
    // Contract ID로 분석 결과 조회
    List<MLAnalysisResult> findByContractId(String contractId);
    
    // raw_ml_response에서 user_id로 분석 결과 조회
    @Query("{'raw_ml_response.user_id': ?0}")
    List<MLAnalysisResult> findByRawMlResponseUserId(String userId);
    
    // Contract ID와 raw_ml_response의 user_id로 분석 결과 조회 (보안)
    @Query("{'contractId': ?0, 'raw_ml_response.user_id': ?1}")
    List<MLAnalysisResult> findByContractIdAndRawMlResponseUserId(String contractId, String userId);

    // 특정 사용자의 특정 타입 분석 결과 조회 (raw_ml_response 기반)
    @Query("{'raw_ml_response.user_id': ?0, 'analysisType': ?1}")
    List<MLAnalysisResult> findByRawMlResponseUserIdAndAnalysisType(String userId, String analysisType);
    
    // 상태별 조회
    List<MLAnalysisResult> findByStatus(String status);
    
    // 날짜 범위로 조회
    List<MLAnalysisResult> findByCreatedDateBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    // raw_ml_response에서 ML Analysis ID로 조회
    @Query("{'raw_ml_response.id': ?0}")
    Optional<MLAnalysisResult> findByRawMlResponseId(Integer mlAnalysisId);
    
    // 성공한 분석 결과만 조회
    List<MLAnalysisResult> findByStatusAndContractId(String status, String contractId);
} 