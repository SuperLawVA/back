// WordsSearchResponseDto.java
package com.superlawva.domain.words.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WordsSearchResponseDto {
    
    private long totalResults;
    private int page;
    private int pageSize;
    private List<WordsDto> data;
}
