package com.superlawva.domain.ml.dto;

import lombok.Data;
import java.util.List;

@Data
public class CertificateUpdateRequest {
    private String title;
    private String userQuery;
    private String body;
    private String strategySummary;
    private String followupStrategy;
    private CertificateResponse.ReceiverDto receiver;
    private CertificateResponse.SenderDto sender;
    private List<CertificateResponse.LegalBasisDto> legalBasis;
    private List<CertificateResponse.CaseBasisDto> caseBasis;
    private CertificateResponse.CertificationMetadataDto certificationMetadata;
} 