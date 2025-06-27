package com.superlawva.domain.search.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;

/**
 * 외부 ML 검색 API의 응답을 매핑하기 위한 DTO
 */
public record MLSearchResponse(
        @JsonProperty("documents") List<Document> documents,
        @JsonProperty("total_results") Integer totalResults
) {
    public record Document(
            @JsonProperty("document") String document,
            @JsonProperty("similarity") Double similarity,
            @JsonProperty("boosted_similarity") Double boostedSimilarity,
            @JsonProperty("source") String source,
            @JsonProperty("metadata") Map<String, Object> metadata
    ) {}
} 