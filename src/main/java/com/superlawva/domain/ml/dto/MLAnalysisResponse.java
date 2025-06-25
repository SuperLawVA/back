package com.superlawva.domain.ml.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MLAnalysisResponse {

    @JsonProperty("id")
    private Integer id; // ML API에서 생성한 분석 ID

    @JsonProperty("user_id")
    private Integer userId;

    @JsonProperty("contract_id")
    private Integer contractId; // ML API에서 0으로 반환되는 문제 있음

    @JsonProperty("created_date")
    private String createdDate; // ISO 8601 형식

    @JsonProperty("articles")
    private List<ArticleAnalysis> articles;

    @JsonProperty("agreements")
    private List<AgreementAnalysis> agreements;

    @JsonProperty("recommended_agreements")
    private List<String> recommendedAgreements;

    @JsonProperty("analysis_metadata")
    private AnalysisMetadata analysisMetadata;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ArticleAnalysis {
        @JsonProperty("result")
        private Boolean result; // true: 문제없음, false: 문제있음

        @JsonProperty("content")
        private String content; // 원본 조항 내용

        @JsonProperty("reason")
        private String reason; // 분석 이유/설명

        @JsonProperty("suggested_revision")
        private String suggestedRevision; // 수정 제안 (문제 있을 때만)

        @JsonProperty("negotiation_points")
        private String negotiationPoints; // 협상 포인트 (문제 있을 때만)

        @JsonProperty("legal_basis")
        private LegalBasis legalBasis; // 법적 근거

        @JsonProperty("case_basis")
        private List<CaseBasis> caseBasis; // 판례 근거
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AgreementAnalysis {
        @JsonProperty("result")
        private Boolean result; // true: 문제없음, false: 문제있음

        @JsonProperty("content")
        private String content; // 원본 특약 내용

        @JsonProperty("reason")
        private String reason; // 분석 이유/설명

        @JsonProperty("suggested_revision")
        private String suggestedRevision; // 수정 제안 (문제 있을 때만)

        @JsonProperty("negotiation_points")
        private String negotiationPoints; // 협상 포인트 (문제 있을 때만)

        @JsonProperty("legal_basis")
        private LegalBasis legalBasis; // 법적 근거

        @JsonProperty("case_basis")
        private List<CaseBasis> caseBasis; // 판례 근거
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class LegalBasis {
        @JsonProperty("law_id")
        private Integer lawId;

        @JsonProperty("law")
        private String law; // 법령명

        @JsonProperty("explanation")
        private String explanation; // 법령 설명

        @JsonProperty("content")
        private String content; // 조문 내용
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CaseBasis {
        @JsonProperty("case_id")
        private Integer caseId;

        @JsonProperty("case")
        private String caseName; // 판례명

        @JsonProperty("explanation")
        private String explanation; // 판례 설명

        @JsonProperty("link")
        private String link; // 판례 링크
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AnalysisMetadata {
        @JsonProperty("model")
        private String model; // "Claude Sonnet 4"

        @JsonProperty("generation_time")
        private Double generationTime; // 처리 시간 (초)

        @JsonProperty("user_agent")
        private String userAgent; // "Mozilla"

        @JsonProperty("version")
        private String version; // "v1.2.3"
    }
}