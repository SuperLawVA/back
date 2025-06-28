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

        ObjectMapper mapper = new ObjectMapper();
        CertificateResponse response = new CertificateResponse();

        // 기본 필드들
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

        // JSON 필드들 파싱
        try {
            if (entity.getReceiverJson() != null) {
                response.setReceiver(mapper.readValue(entity.getReceiverJson(), ReceiverDto.class));
            }
            if (entity.getSenderJson() != null) {
                response.setSender(mapper.readValue(entity.getSenderJson(), SenderDto.class));
            }
            if (entity.getLegalBasisJson() != null) {
                response.setLegalBasis(mapper.readValue(entity.getLegalBasisJson(), 
                    new TypeReference<List<LegalBasisDto>>() {}));
            }
            if (entity.getCaseBasisJson() != null) {
                response.setCaseBasis(mapper.readValue(entity.getCaseBasisJson(), 
                    new TypeReference<List<CaseBasisDto>>() {}));
            }
            if (entity.getCertificationMetadataJson() != null) {
                response.setCertificationMetadata(mapper.readValue(entity.getCertificationMetadataJson(), 
                    CertificationMetadataDto.class));
            }
        } catch (Exception e) {
            log.warn("JSON 파싱 실패: {}", e.getMessage());
            // 기본값들 설정
            if (response.getLegalBasis() == null) response.setLegalBasis(Collections.emptyList());
            if (response.getCaseBasis() == null) response.setCaseBasis(Collections.emptyList());
        }

        return response;
    }
}