//package com.superlawva.domain.ml.dto;
//
//import com.fasterxml.jackson.annotation.JsonProperty;
//import lombok.AllArgsConstructor;
//import lombok.Builder;
//import lombok.Data;
//import lombok.NoArgsConstructor;
//
//import java.time.LocalDateTime;
//import java.util.List;
//import java.util.Map;
//
//@Data
//@NoArgsConstructor
//@AllArgsConstructor
//@Builder
//public class MLAnalysisResponse {
//
//    @JsonProperty("success")
//    private boolean success;
//
//    @JsonProperty("message")
//    private String message;
//
//    @JsonProperty("contract_id")
//    private String contractId;
//
//    @JsonProperty("user_id")
//    private String userId;
//
//    @JsonProperty("request_type")
//    private String requestType;
//
//    @JsonProperty("analysis_result")
//    private AnalysisResult analysisResult;
//
//    @JsonProperty("generated_content")
//    private GeneratedContent generatedContent;
//
//    @JsonProperty("processing_info")
//    private ProcessingInfo processingInfo;
//
//    @Data
//    @NoArgsConstructor
//    @AllArgsConstructor
//    @Builder
//    public static class AnalysisResult {
//        @JsonProperty("risk_score")
//        private Double riskScore; // 0.0 ~ 1.0
//
//        @JsonProperty("risk_level")
//        private String riskLevel; // "LOW", "MEDIUM", "HIGH", "CRITICAL"
//
//        @JsonProperty("risk_factors")
//        private List<String> riskFactors;
//
//        @JsonProperty("legal_issues")
//        private List<LegalIssue> legalIssues;
//
//        @JsonProperty("recommendations")
//        private List<String> recommendations;
//
//        @JsonProperty("compliance_check")
//        private ComplianceCheck complianceCheck;
//
//        @JsonProperty("summary")
//        private String summary;
//    }
//
//    @Data
//    @NoArgsConstructor
//    @AllArgsConstructor
//    @Builder
//    public static class LegalIssue {
//        @JsonProperty("category")
//        private String category; // "PAYMENT", "TERMINATION", "LIABILITY", etc.
//
//        @JsonProperty("severity")
//        private String severity; // "LOW", "MEDIUM", "HIGH", "CRITICAL"
//
//        @JsonProperty("description")
//        private String description;
//
//        @JsonProperty("clause_reference")
//        private String clauseReference;
//
//        @JsonProperty("suggestion")
//        private String suggestion;
//    }
//
//    @Data
//    @NoArgsConstructor
//    @AllArgsConstructor
//    @Builder
//    public static class ComplianceCheck {
//        @JsonProperty("is_compliant")
//        private boolean isCompliant;
//
//        @JsonProperty("violations")
//        private List<String> violations;
//
//        @JsonProperty("missing_clauses")
//        private List<String> missingClauses;
//
//        @JsonProperty("regulatory_issues")
//        private List<String> regulatoryIssues;
//    }
//
//    @Data
//    @NoArgsConstructor
//    @AllArgsConstructor
//    @Builder
//    public static class GeneratedContent {
//        @JsonProperty("content_type")
//        private String contentType; // "PROOF_OF_CONTENT", "LEGAL_ANALYSIS", "CONTRACT_SUMMARY"
//
//        @JsonProperty("title")
//        private String title;
//
//        @JsonProperty("content")
//        private String content;
//
//        @JsonProperty("sections")
//        private List<ContentSection> sections;
//
//        @JsonProperty("templates_used")
//        private List<String> templatesUsed;
//
//        @JsonProperty("confidence_score")
//        private Double confidenceScore; // 0.0 ~ 1.0
//    }
//
//    @Data
//    @NoArgsConstructor
//    @AllArgsConstructor
//    @Builder
//    public static class ContentSection {
//        @JsonProperty("section_name")
//        private String sectionName;
//
//        @JsonProperty("section_content")
//        private String sectionContent;
//
//        @JsonProperty("section_type")
//        private String sectionType; // "HEADER", "BODY", "FOOTER", "SIGNATURE"
//    }
//
//    @Data
//    @NoArgsConstructor
//    @AllArgsConstructor
//    @Builder
//    public static class ProcessingInfo {
//        @JsonProperty("model_name")
//        private String modelName;
//
//        @JsonProperty("model_version")
//        private String modelVersion;
//
//        @JsonProperty("processing_time_seconds")
//        private Double processingTimeSeconds;
//
//        @JsonProperty("token_count")
//        private Integer tokenCount;
//
//        @JsonProperty("quality_score")
//        private Double qualityScore; // 0.0 ~ 1.0
//
//        @JsonProperty("processed_at")
//        private String processedAt;
//
//        @JsonProperty("additional_metadata")
//        private Map<String, Object> additionalMetadata;
//    }
//}