package com.superlawva.domain.ml.dto;

import com.superlawva.domain.ml.entity.CertificateEntity;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Collections;

@Data
@Slf4j
public class CertificateResponse {

    private String id;
    private Integer mlCertificateId;
    private String contractId;
    private String userId;
    private String userQuery;
    private LocalDateTime createdDate;
    private String mlCreatedDate;

    // 내용증명서 내용
    private String title;
    private ReceiverDto receiver;
    private SenderDto sender;
    private String body;
    
    @JsonProperty("strategy_summary")
    private String strategySummary;
    
    @JsonProperty("followup_strategy")
    private String followupStrategy;
    
    private List<LegalBasisDto> legalBasis;
    private List<CaseBasisDto> caseBasis;
    private CertificationMetadataDto certificationMetadata;

    private String status;
    private String errorMessage;

    @Data
    public static class ReceiverDto {
        private String name;
        private String address;
        private String detailAddress;
    }

    @Data
    public static class SenderDto {
        private String name;
        private String address;
        private String detailAddress;
    }

    @Data
    public static class LegalBasisDto {
        private Integer lawId;
        private String law;
        private String explanation;
        private String content;
    }

    @Data
    public static class CaseBasisDto {
        private Integer caseId;
        private String caseName;
        private String explanation;
        private String link;
    }

    @Data
    public static class CertificationMetadataDto {
        private String model;
        private Double generationTime;
        private String userAgent;
        private String version;
    }

    /**
     * CertificateEntity를 CertificateResponse로 변환
     */
    public static CertificateResponse fromEntity(CertificateEntity entity) {
        if (entity == null) return null;

        CertificateResponse response = new CertificateResponse();

        // 기본 필드들만 복사합니다. JSON 파싱은 서비스 레이어에서 처리합니다.
        response.setId(entity.getId().toString());
        response.setMlCertificateId(entity.getMlCertificateId());
        response.setContractId(entity.getContractId());
        response.setUserId(entity.getUserId());
        response.setUserQuery(entity.getUserQuery());
        response.setCreatedDate(entity.getCreatedDate());
        response.setMlCreatedDate(entity.getMlCreatedDate());
        response.setTitle(entity.getTitle());
        response.setBody(entity.getBody());
        response.setStrategySummary(entity.getStrategySummary());
        response.setFollowupStrategy(entity.getFollowupStrategy());
        response.setStatus(entity.getStatus());
        response.setErrorMessage(entity.getErrorMessage());

        // JSON 필드들은 기본값으로 초기화
        response.setLegalBasis(Collections.emptyList());
        response.setCaseBasis(Collections.emptyList());

        return response;
    }
}