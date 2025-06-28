package com.superlawva.domain.ml.dto;

import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@Data
@Schema(description = "계약서 수정 요청 DTO", example = "{\"articles\": [\"제1조 임대목적\", \"제2조 임대기간\"], \"userQuery\": [\"반려동물 허용\", \"주차공간 필요\"], \"contractType\": \"월세\"}")
public class ContractUpdateRequest {
    
    @JsonProperty("articles")
    @Schema(description = "계약 조항 목록 (선택)", example = "[\"제1조 임대목적\", \"제2조 임대기간\"]")
    private List<String> articles;
    
    @JsonProperty("userQuery")
    @Schema(description = "사용자 쿼리 (선택)", example = "[\"반려동물 허용\", \"주차공간 필요\"]")
    private List<String> userQuery;
    
    @JsonProperty("contractType")
    @Schema(description = "계약 유형 (선택)", example = "월세", allowableValues = {"전세", "월세"})
    private String contractType;
    
    @JsonProperty("agreements")
    @Schema(description = "특약사항 목록 (선택)", example = "[\"반려동물 허용\", \"주차공간 제공\"]")
    private List<String> agreements;
} 