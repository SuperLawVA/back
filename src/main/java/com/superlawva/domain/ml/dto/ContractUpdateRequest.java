package com.superlawva.domain.ml.dto;

import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
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
    
    @JsonProperty("baseArticles")
    @Schema(description = "기본 계약 조항 수정 (선택) - 기존 조항을 모두 대체합니다", 
            example = "[\"제1조 (수정) 수정된 계약 목적\", \"제2조 (수정) 수정된 임대료 조건\"]")
    @Size(max = 50, message = "기본 조항은 최대 50개까지 입력 가능합니다.")
    private List<String> baseArticles;
    
    @JsonProperty("requirements")
    @Schema(description = "특별 요구사항 수정 (선택) - 기존 요구사항을 모두 대체합니다", 
            example = "[\"펜트하우스 이용 허용\", \"주차 2대 가능\"]")
    @Size(max = 10, message = "요구사항은 최대 10개까지 입력 가능합니다.")
    private List<String> requirements;
    
    @JsonProperty("specialTerms")
    @Schema(description = "특약사항 수정 (선택) - 기존 특약사항을 모두 대체합니다", 
            example = "[\"반려동물 사육 허용\", \"소음 시간 제한: 오후 10시 이후 금지\"]")
    @Size(max = 20, message = "특약사항은 최대 20개까지 입력 가능합니다.")
    private List<String> specialTerms;
    
    @JsonProperty("propertyInfo")
    @Schema(description = "부동산 정보 수정 (선택)", implementation = PropertyInfoUpdate.class)
    private PropertyInfoUpdate propertyInfo;
    
    @JsonProperty("parties")
    @Schema(description = "계약 당사자 정보 수정 (선택)", implementation = ContractPartiesUpdate.class)
    private ContractPartiesUpdate parties;

    @Data
    @Schema(description = "부동산 정보 수정")
    public static class PropertyInfoUpdate {
        @Schema(description = "부동산 주소", example = "서울시 서초구 서초대로 456")
        private String address;
        
        @Schema(description = "부동산 유형", example = "오피스텔", allowableValues = {"아파트", "오피스텔", "단독주택", "상가", "기타"})
        private String propertyType;
        
        @Schema(description = "면적 (㎡)", example = "95.2")
        private Double area;
        
        @Schema(description = "층수", example = "12")
        private Integer floor;
    }

    @Data
    @Schema(description = "계약 당사자 정보 수정")
    public static class ContractPartiesUpdate {
        @Schema(description = "임대인(집주인) 이름", example = "이영희")
        private String lessorName;
        
        @Schema(description = "임대인 연락처", example = "010-1111-2222")
        private String lessorPhone;
        
        @Schema(description = "임차인(세입자) 이름", example = "박민수")
        private String lesseeName;
        
        @Schema(description = "임차인 연락처", example = "010-3333-4444")
        private String lesseePhone;
    }
} 