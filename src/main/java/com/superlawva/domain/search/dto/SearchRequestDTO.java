package com.superlawva.domain.search.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "AI 기반 법령 및 판례 검색 요청 DTO")
public record SearchRequestDTO(
        
        @Schema(description = "검색할 키워드 또는 질문. 자연어 문장 형식도 가능합니다.", example = "임대차 계약 시 보증금 반환 조건")
        @NotBlank(message = "검색어는 필수입니다.")
        @Size(max = 500, message = "검색어는 500자 이하여야 합니다.")
        String query,
        
        @Schema(description = "검색 대상 유형. 'law'는 법령, 'case'는 판례, 'both'는 둘 다를 의미합니다.", example = "both", allowableValues = {"law", "case", "both"})
        String search_type,
        
        @Schema(description = "유사도 기준으로 상위 몇 개의 결과를 가져올지 지정합니다. (ML 모델에 전달)", example = "10")
        @Min(value = 1, message = "최소 1개 이상 검색해야 합니다.")
        @Max(value = 20, message = "최대 20개까지 검색 가능합니다.")
        Integer k,

        @Schema(description = "결과 목록의 페이지 번호. 1부터 시작합니다.", example = "1", defaultValue = "1")
        Integer page,

        @Schema(description = "한 페이지에 표시할 결과의 수.", example = "10", defaultValue = "10")
        Integer pageSize
) {
    
    public SearchRequestDTO {
        // 기본값 설정
        if (search_type == null || search_type.isBlank()) {
            search_type = "both";
        }
        if (k == null || k <= 0) {
            k = 10;
        }
        if (page == null || page < 1) {
            page = 1;
        }
        if (pageSize == null || pageSize < 1) {
            pageSize = 10;
        }
    }
} 