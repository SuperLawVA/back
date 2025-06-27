package com.superlawva.domain.ml.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Document(collection = "certificate")
public class Certificate {

    @Id
    private String id;

    @Field("ml_certificate_id")
    private Integer mlCertificateId; // ML API에서 반환하는 ID

    @Field("contract_id")
    private String contractId;

    @Field("user_id")
    private String userId;

    @Field("user_query")
    private String userQuery; // 사용자가 입력한 요청사항

    @Field("created_date")
    private LocalDateTime createdDate;

    @Field("ml_created_date")
    private String mlCreatedDate; // ML API에서 반환하는 생성일 (원본 형식 보존)

    // ML API 응답 - 내용증명서 기본 정보
    private String title; // 내용증명서 제목

    // ML API 응답 - 수신인 정보
    private Receiver receiver;

    // ML API 응답 - 발신인 정보
    private Sender sender;

    // ML API 응답 - 내용증명서 본문
    private String body;

    // ML API 응답 - 전략 요약
    @Field("strategy_summary")
    private String strategySummary;

    // ML API 응답 - 후속 전략
    @Field("followup_strategy")
    private String followupStrategy;

    // ML API 응답 - 법적 근거들
    @Field("legal_basis")
    private List<LegalBasis> legalBasis;

    // ML API 응답 - 판례 근거들
    @Field("case_basis")
    private List<CaseBasis> caseBasis;

    // ML API 응답 메타데이터
    @Field("certification_metadata")
    private CertificationMetadata certificationMetadata;

    // 처리 상태
    private String status; // "SUCCESS", "FAILED", "PROCESSING"

    // 오류 정보 (실패 시)
    @Field("error_message")
    private String errorMessage;

    @Data
    public static class Receiver {
        private String name;
        private String address;
        @Field("detail_address")
        private String detailAddress;
    }

    @Data
    public static class Sender {
        private String name;
        private String address;
        @Field("detail_address")
        private String detailAddress;
    }

    @Data
    public static class LegalBasis {
        @Field("law_id")
        private Integer lawId;
        private String law;
        private String explanation;
        private String content;
    }

    @Data
    public static class CaseBasis {
        @Field("case_id")
        private Integer caseId;
        @Field("case")
        private String caseName;
        private String explanation;
        private String link;
    }

    @Data
    public static class CertificationMetadata {
        private String model; // 사용된 AI 모델
        @Field("generation_time")
        private Double generationTime; // 생성 시간 (초)
        @Field("user_agent")
        private String userAgent;
        private String version; // ML API 버전
    }
} 