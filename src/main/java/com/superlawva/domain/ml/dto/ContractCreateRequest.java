package com.superlawva.domain.ml.dto;

import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@Data
@Schema(description = "계약서 직접 작성 및 특약 생성 요청 DTO")
public class ContractCreateRequest {
    @JsonProperty("userId")
    @Schema(description = "사용자를 식별하는 고유 ID", required = true, example = "user-12345")
    @NotNull(message = "userId는 필수입니다.")
    private String userId;
    
    @JsonProperty("title")
    @Schema(description = "계약서 제목", example = "우리집 임대차 계약서")
    @Size(max = 100, message = "제목은 100자를 초과할 수 없습니다.")
    private String title;
    
    @JsonProperty("contractType")
    @Schema(description = "계약서 유형", example = "MONTHLY_RENT", allowableValues = {"JEONSE", "MONTHLY_RENT", "SALE", "CUSTOM"})
    @NotNull(message = "계약서 유형은 필수입니다.")
    private String contractType;

    @JsonProperty("userQuery")
    @Schema(
        description = "AI 특약 생성을 위한 사용자의 요구사항. 자유로운 형식의 문장으로 요청합니다.", 
        required = true,
        example = "[\"전세 사기가 걱정되니, 보증금을 안전하게 지킬 수 있는 조항을 추가해주세요.\", \"집주인이 바뀌더라도 임대차 계약이 유지되도록 하는 내용도 포함해주세요.\"]"
    )
    @NotEmpty(message = "userQuery는 1개 이상 입력해야 합니다.")
    private List<String> userQuery;

    @JsonProperty("articles")
    @Schema(
        description = "사용자가 직접 입력하는 계약서의 기본 조항 목록입니다.", 
        example = "[\"제1조 (목적) 본 계약은 ...\", \"제2조 (보증금) 임차인은 보증금 1억 원을 ...\"]"
    )
    private List<String> articles;
    
    @JsonProperty("propertyInfo")
    @Schema(description = "부동산 정보 (선택사항)", implementation = PropertyInfo.class)
    private PropertyInfo propertyInfo;
    
    @JsonProperty("parties")
    @Schema(description = "계약 당사자 정보 (선택사항)", implementation = ContractParties.class)
    private ContractParties parties;

    @Data
    @Schema(description = "부동산 기본 정보")
    public static class PropertyInfo {
        @Schema(description = "부동산 주소", example = "서울시 강남구 테헤란로 123")
        private String address;
        
        @Schema(description = "부동산 유형", example = "아파트", allowableValues = {"아파트", "오피스텔", "단독주택", "상가", "기타"})
        private String propertyType;
        
        @Schema(description = "면적 (㎡)", example = "84.5")
        private Double area;
        
        @Schema(description = "층수", example = "5")
        private Integer floor;
    }

    @Data
    @Schema(description = "계약 당사자 정보")
    public static class ContractParties {
        @Schema(description = "임대인(집주인) 이름", example = "홍길동")
        private String lessorName;
        
        @Schema(description = "임대인 연락처", example = "010-1234-5678")
        private String lessorPhone;
        
        @Schema(description = "임차인(세입자) 이름", example = "김철수")
        private String lesseeName;
        
        @Schema(description = "임차인 연락처", example = "010-9876-5432")
        private String lesseePhone;
    }
}