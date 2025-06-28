package com.superlawva.domain.ml.dto;

import com.superlawva.domain.ocr3.entity.ContractData;
import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

@Data
@Schema(description = "계약서 생성/조회 응답 DTO (ML API 전체 구조 반영)")
public class ContractResponse {
    @Schema(description = "계약서 ID")
    private String id;
    @Schema(description = "사용자 ID")
    private String userId;
    @Schema(description = "계약서 종류")
    private String contractType;
    @Schema(description = "계약 조항 목록")
    private List<String> articles;
    @Schema(description = "추천 특약사항 목록")
    private List<RecommendedAgreementDto> recommendedAgreements;
    @Schema(description = "법적 근거 목록")
    private List<LegalBasisDto> legalBasis;
    @Schema(description = "판례 근거 목록")
    private List<CaseBasisDto> caseBasis;
    @Schema(description = "ML 분석 메타데이터")
    private AnalysisMetadataDto analysisMetadata;
    @Schema(description = "생성일")
    private LocalDateTime createdDate;
    @Schema(description = "수정일")
    private LocalDateTime modifiedDate;

    @Data
    public static class RecommendedAgreementDto {
        private String reason;
        private String suggestedRevision;
        private String negotiationPoints;
        private LegalBasisDto legalBasis;
        private List<CaseBasisDto> caseBasis;
    }
    @Data
    public static class LegalBasisDto {
        private Long lawId;
        private String law;
        private String explanation;
        private String content;
    }
    @Data
    public static class CaseBasisDto {
        private Long caseId;
        private String caseName;
        private String explanation;
        private String link;
    }
    @Data
    public static class AnalysisMetadataDto {
        private String model;
        private String version;
        private Double generationTime;
    }

    public static ContractResponse fromEntity(ContractData entity) {
        ContractResponse dto = new ContractResponse();
        dto.setId(entity.getId().toString());
        dto.setUserId(entity.getUserId());
        dto.setContractType(entity.getContractType());
        dto.setArticles(new ArrayList<>());
        dto.setCreatedDate(entity.getCreatedDate());
        dto.setModifiedDate(entity.getModifiedDate());
        return dto;
    }
}