package com.superlawva.domain.ml.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotEmpty;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "ML 계약서 분석 요청 DTO - 프론트엔드 친화적")
public class MLAnalysisRequest {

    @JsonProperty("contractId")
    @Schema(description = "분석할 계약서 ID", example = "123", required = true)
    @NotNull(message = "계약서 ID는 필수입니다.")
    private String contractId;

    @JsonProperty("analysisType")
    @Schema(
        description = "분석 유형을 선택합니다.", 
        example = "COMPREHENSIVE",
        allowableValues = {"RISK_ANALYSIS", "COMPLIANCE_CHECK", "COMPREHENSIVE", "QUICK_REVIEW"},
        required = true
    )
    @NotEmpty(message = "분석 유형은 필수입니다.")
    private String analysisType;

    @JsonProperty("focusAreas")
    @Schema(
        description = "집중 분석 영역 (선택사항). 특정 영역에 대한 상세 분석을 원할 때 사용합니다.",
        example = "[\"보증금 관련\", \"계약 기간\", \"특약사항\"]"
    )
    private java.util.List<String> focusAreas;

    @JsonProperty("options")
    @Schema(description = "추가 분석 옵션", implementation = AnalysisOptions.class)
    private AnalysisOptions options;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "분석 옵션 설정")
    public static class AnalysisOptions {
        
        @JsonProperty("includeRecommendations")
        @Schema(description = "개선사항 추천 포함 여부", example = "true", defaultValue = "true")
        @Builder.Default
        private Boolean includeRecommendations = true;

        @JsonProperty("includeLegalBasis")
        @Schema(description = "법적 근거 포함 여부", example = "true", defaultValue = "true")
        @Builder.Default
        private Boolean includeLegalBasis = true;

        @JsonProperty("includeCaseBasis")
        @Schema(description = "판례 근거 포함 여부", example = "false", defaultValue = "false")
        @Builder.Default
        private Boolean includeCaseBasis = false;

        @JsonProperty("detailLevel")
        @Schema(
            description = "분석 상세도 레벨", 
            example = "DETAILED",
            allowableValues = {"SUMMARY", "DETAILED", "EXPERT"},
            defaultValue = "DETAILED"
        )
        @Builder.Default
        private String detailLevel = "DETAILED";

        @JsonProperty("priority")
        @Schema(
            description = "분석 우선순위", 
            example = "NORMAL",
            allowableValues = {"LOW", "NORMAL", "HIGH", "URGENT"},
            defaultValue = "NORMAL"
        )
        @Builder.Default
        private String priority = "NORMAL";
    }
}