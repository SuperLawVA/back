package com.superlawva.domain.ml.dto;

import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@Data
@Schema(description = "계약서 수정 요청 DTO (직접 작성 / OCR 공통)")
public class ContractUpdateRequest {

    // --- 기본 및 간단 필드 ---
    @JsonProperty("contractType")
    @Schema(description = "계약서 종류 (선택)")
    private String contractType;

    @JsonProperty("articles")
    @Schema(description = "계약 조항 목록 (선택)")
    private List<String> articles;

    @JsonProperty("agreements")
    @Schema(description = "특약사항 목록 (선택)")
    private List<String> agreements;

    @JsonProperty("recommendedAgreements")
    @Schema(description = "추천 특약사항 목록 (선택)")
    private List<com.superlawva.domain.ml.dto.ContractResponse.RecommendedAgreementDto> recommendedAgreements;

    @JsonProperty("legalBasis")
    @Schema(description = "법적 근거 목록 (선택)")
    private List<com.superlawva.domain.ml.dto.ContractResponse.LegalBasisDto> legalBasis;

    @JsonProperty("caseBasis")
    @Schema(description = "판례 근거 목록 (선택)")
    private List<com.superlawva.domain.ml.dto.ContractResponse.CaseBasisDto> caseBasis;



    // --- 복합(중첩) 필드 ---
    @JsonProperty("dates")
    private com.superlawva.domain.ocr3.entity.ContractData.Dates dates;

    @JsonProperty("property")
    private com.superlawva.domain.ocr3.entity.ContractData.Property property;

    @JsonProperty("payment")
    private com.superlawva.domain.ocr3.entity.ContractData.Payment payment;

    @JsonProperty("lessor")
    private com.superlawva.domain.ocr3.entity.ContractData.Party lessor;

    @JsonProperty("lessee")
    private com.superlawva.domain.ocr3.entity.ContractData.Party lessee;

    @JsonProperty("broker1")
    private com.superlawva.domain.ocr3.entity.ContractData.Broker broker1;

    @JsonProperty("broker2")
    private com.superlawva.domain.ocr3.entity.ContractData.Broker broker2;

} 