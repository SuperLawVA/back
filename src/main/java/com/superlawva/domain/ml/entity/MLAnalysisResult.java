package com.superlawva.domain.ml.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Document(collection = "analysis")
public class MLAnalysisResult {

    @Id
    private String id;

    @Field("contract_id")
    private String contractId;

    @Field("user_id")
    private String userId;

    @Field("analysis_type")
    private String analysisType; // "CONTRACT_ANALYSIS", "PROOF_GENERATION", "SPECIAL_TERMS"

    @Field("created_date")
    private LocalDateTime createdDate;

    // 추천 특약사항들 (특약사항 생성 시)
    @Field("recommended_agreements")
    private List<RecommendedAgreement> recommendedAgreements;

    // 생성된 내용증명서 내용 (내용증명서 생성 시)
    @Field("proof_content")
    private String proofContent;

    // 전체 ML API 응답 (원본 보존)
    @Field("raw_ml_response")
    private Map<String, Object> rawMlResponse;

    // 처리 상태
    private String status; // "SUCCESS", "FAILED", "PROCESSING"

    // 오류 정보 (실패 시)
    @Field("error_message")
    private String errorMessage;

    @Data
    public static class AnalyzedArticle {
        private Boolean result; // 위험성 여부
        private String content; // 조항 내용
        private String reason; // 분석 이유
        @Field("suggested_revision")
        private String suggestedRevision; // 수정 제안
        @Field("negotiation_points")
        private String negotiationPoints; // 협상 포인트
    }

    @Data
    public static class AnalyzedAgreement {
        private Boolean result; // 위험성 여부
        private String content; // 특약 내용
        private String reason; // 분석 이유
        @Field("suggested_revision")
        private String suggestedRevision; // 수정 제안
        @Field("negotiation_points")
        private String negotiationPoints; // 협상 포인트
    }

    @Data
    public static class RecommendedAgreement {
        private String content; // 추천 특약 내용
        private String reason; // 추천 이유
        private String category; // 카테고리
        private Integer priority; // 우선순위
    }

    @Data
    public static class AnalysisMetadata {
        private String model; // 사용된 AI 모델
        @Field("generation_time")
        private Double generationTime; // 생성 시간 (초)
        private String version; // ML API 버전
        @Field("processing_steps")
        private List<String> processingSteps; // 처리 단계들
    }
} 