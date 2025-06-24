package com.superlawva.domain.document.dto;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContractGenerationRequestDTO {
    
    @NotNull(message = "사용자 ID는 필수입니다")
    private Long userId;
    
    @NotBlank(message = "계약 유형은 필수입니다")
    private String contractType; // "전세", "월세"
    
    // 부동산 정보
    @NotBlank(message = "부동산 주소는 필수입니다")
    private String propertyAddress;
    
    private String detailAddress;
    
    private BigDecimal propertyArea; // ㎡
    
    private String buildingType; // "아파트", "오피스텔", "단독주택" 등
    
    // 계약 조건
    @NotNull(message = "보증금은 필수입니다")
    private BigDecimal depositAmount;
    
    private BigDecimal monthlyRent; // 월세인 경우
    
    @NotNull(message = "계약 시작일은 필수입니다")
    private LocalDate contractStartDate;
    
    @NotNull(message = "계약 종료일은 필수입니다")
    private LocalDate contractEndDate;
    
    // 임대인/임차인 정보
    private String lessorName;
    private String lessorPhone;
    private String lessorAddress;
    
    private String lesseeName;  
    private String lesseePhone;
    private String lesseeAddress;
    
    // 특별 요구사항
    private List<String> specialRequirements; // 사용자가 추가하고 싶은 특약사항
    
    private String additionalNotes; // 추가 메모
    
    // AI 생성 옵션
    @Builder.Default
    private boolean includeStandardClauses = true; // 표준 조항 포함 여부
    
    @Builder.Default
    private boolean includeRecommendedClauses = true; // AI 추천 조항 포함 여부
} 