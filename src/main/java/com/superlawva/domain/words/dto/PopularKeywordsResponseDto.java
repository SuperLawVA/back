package com.superlawva.domain.words.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "인기 검색어 응답 DTO")
public class PopularKeywordsResponseDto {

    @Schema(description = "인기 검색어 목록", example = "[\"보증금\", \"임대차계약\", \"계약서\"]")
    private List<String> keywords;
}