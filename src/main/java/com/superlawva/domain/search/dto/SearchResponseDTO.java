package com.superlawva.domain.search.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@Schema(description = "법령/판례 검색 응답 (ML 팀 API 스펙)")
public record SearchResponseDTO(
        
        @Schema(description = "법령 검색 결과")
        List<DocumentResult> laws,
        
        @Schema(description = "판례 검색 결과")
        List<DocumentResult> cases,
        
        @Schema(description = "전체 검색된 문서 목록")
        List<DocumentResult> documents,
        
        @Schema(description = "검색 처리 시간(초)", example = "0.245")
        @JsonProperty("search_time_seconds")
        Double searchTimeSeconds,
        
        @Schema(description = "총 검색 결과 개수", example = "15")
        @JsonProperty("total_results")
        Integer totalResults
) {
    
    @Schema(description = "검색된 개별 문서")
    public record DocumentResult(
            
            @Schema(description = "문서 제목", example = "주택임대차보호법 제3조")
            String title,
            
            @Schema(description = "문서 내용", example = "임대차 계약에 있어서...")
            String content,
            
            @Schema(description = "유사도 점수", example = "0.892")
            Double similarity,
            
            @Schema(description = "문서 메타데이터")
            DocumentMetadata metadata
    ) {}
    
    @Schema(description = "문서 메타데이터")
    public record DocumentMetadata(
            
            @Schema(description = "문서 유형", example = "law")
            String type,
            
            @Schema(description = "법령/사건 번호", example = "주택임대차보호법")
            String source,
            
            @Schema(description = "조항/판결일", example = "제3조")
            String section,
            
            @Schema(description = "URL 또는 참조", example = "https://...")
            String url,
            
            @Schema(description = "판례 고유 ID (판례인 경우만)", example = "2023다12345")
            String caseId
    ) {}
} 