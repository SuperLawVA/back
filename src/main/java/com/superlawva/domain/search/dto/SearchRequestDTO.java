package com.superlawva.domain.search.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "법령/판례 검색 요청 (ML 팀 API 스펙)")
public record SearchRequestDTO(
        
        @Schema(description = "검색 질의", example = "임대차 보증금 반환")
        @NotBlank(message = "검색어는 필수입니다.")
        @Size(max = 500, message = "검색어는 500자 이하여야 합니다.")
        String query,
        
        @Schema(description = "검색 유형", example = "both", allowableValues = {"law", "case", "both"})
        String search_type,
        
        @Schema(description = "검색 결과 개수", example = "10")
        @Min(value = 1, message = "최소 1개 이상 검색해야 합니다.")
        @Max(value = 20, message = "최대 20개까지 검색 가능합니다.")
        Integer k,

        @Schema(description = "페이지 번호", example = "1", defaultValue = "1")
        Integer page,

        @Schema(description = "페이지당 결과 수", example = "10", defaultValue = "10")
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