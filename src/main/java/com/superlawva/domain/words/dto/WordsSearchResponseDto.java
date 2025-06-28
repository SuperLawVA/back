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
@Schema(description = "법률 용어 검색 응답 DTO")
public class WordsSearchResponseDto {

    @Schema(description = "전체 검색 결과 수", example = "25")
    private long totalResults;
    
    @Schema(description = "현재 페이지 번호", example = "1")
    private int page;
    
    @Schema(description = "페이지당 결과 수", example = "10")
    private int pageSize;
    
    @Schema(description = "검색된 용어 목록")
    private List<WordsDto> data;
}