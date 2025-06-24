//package com.superlawva.domain.ml.dto;
//
//import com.fasterxml.jackson.annotation.JsonProperty;
//import lombok.AllArgsConstructor;
//import lombok.Builder;
//import lombok.Data;
//import lombok.NoArgsConstructor;
//
//@Data
//@NoArgsConstructor
//@AllArgsConstructor
//@Builder
//public class MLAnalysisRequest {
//
//    @JsonProperty("contract_id")
//    private String contractId;
//
//    @JsonProperty("user_id")
//    private String userId;
//
//    @JsonProperty("request_type")
//    private String requestType; // "CONTRACT_ANALYSIS" 또는 "PROOF_GENERATION"
//
//    @JsonProperty("contract_data")
//    private ContractDataForML contractData;
//
//    @JsonProperty("additional_params")
//    private AdditionalParams additionalParams;
//
//    @Data
//    @NoArgsConstructor
//    @AllArgsConstructor
//    @Builder
//    public static class ContractDataForML {
//        @JsonProperty("contract_type")
//        private String contractType;
//
//        @JsonProperty("dates")
//        private Object dates;
//
//        @JsonProperty("property")
//        private Object property;
//
//        @JsonProperty("payment")
//        private Object payment;
//
//        @JsonProperty("lessor")
//        private Object lessor;
//
//        @JsonProperty("lessee")
//        private Object lessee;
//
//        @JsonProperty("articles")
//        private Object articles;
//
//        @JsonProperty("agreements")
//        private Object agreements;
//    }
//
//    @Data
//    @NoArgsConstructor
//    @AllArgsConstructor
//    @Builder
//    public static class AdditionalParams {
//        @JsonProperty("analysis_type")
//        private String analysisType; // "COMPREHENSIVE", "RISK_ANALYSIS", "LEGAL_REVIEW"
//
//        @JsonProperty("output_format")
//        private String outputFormat; // "DETAILED", "SUMMARY", "REPORT"
//
//        @JsonProperty("language")
//        private String language; // "ko", "en"
//
//        @JsonProperty("priority")
//        private String priority; // "HIGH", "NORMAL", "LOW"
//    }
//}