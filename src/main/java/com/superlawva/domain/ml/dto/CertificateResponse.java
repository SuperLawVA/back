package com.superlawva.domain.ml.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
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
    private String strategySummary;
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
} 