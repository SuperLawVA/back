package com.superlawva.domain.search.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

@Schema(description = "법령/판례 검색 응답 (클라이언트 전달용)")
public record SearchResponseDTO(
        
        @Schema(description = "법령 검색 결과")
        List<DocumentResult> laws,
        
        @Schema(description = "판례 검색 결과")
        List<DocumentResult> cases,
        
        @Schema(description = "전체 검색된 문서 목록 (하위 호환성용)")
        List<DocumentResult> documents,
        
        @Schema(description = "검색 처리 시간(초)", example = "0.245")
        @JsonProperty("search_time_seconds")
        Double searchTimeSeconds,
        
        @Schema(description = "총 검색 결과 개수", example = "15")
        @JsonProperty("total_results")
        Integer totalResults,

        @Schema(description = "현재 페이지 번호", example = "1")
        @JsonProperty("current_page")
        Integer currentPage,

        @Schema(description = "페이지당 결과 수", example = "10")
        @JsonProperty("page_size")
        Integer pageSize,

        @Schema(description = "총 페이지 수", example = "2")
        @JsonProperty("total_pages")
        Integer totalPages
) {
    
    @Schema(description = "검색된 개별 문서")
    public record DocumentResult(
            
            @Schema(description = "문서 제목", example = "주택임대차보호법 제3조")
            String title,
            
            @Schema(description = "문서 내용", example = "임대차 계약에 있어서...")
            String content,
            
            @Schema(description = "원본 유사도 점수", example = "0.892")
            Double similarity,
            
            @Schema(description = "가중치 적용 유사도 점수", example = "0.912")
            Double boostedSimilarity,
            
            @Schema(description = "문서 출처 (law/case)", example = "law")
            String source,
            
            @Schema(description = "문서 메타데이터")
            Map<String, Object> metadata
    ) {
        @com.fasterxml.jackson.annotation.JsonCreator
        public DocumentResult(
                @JsonProperty("document") String document,
                @JsonProperty("similarity") Double similarity,
                @JsonProperty("boostedSimilarity") Double boostedSimilarity,
                @JsonProperty("source") String source,
                @JsonProperty("metadata") Map<String, Object> metadata
        ) {
            this(parseTitle(document), parseContent(document), similarity, boostedSimilarity, source, metadata);
        }

        private static String parseTitle(String document) {
            if (document == null || !document.contains("\\n")) {
                return document;
            }
            return document.split("\\\\n", 2)[0].replace("제목:", "").trim();
        }

        private static String parseContent(String document) {
            if (document == null || !document.contains("\\n")) {
                return "";
            }
            return document.split("\\\\n", 2)[1].replace("내용:", "").trim();
        }
    }
} 