// PopularKeywordsResponseDto.java
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
public class PopularKeywordsResponseDto {
    
    private List<String> keywords;
}
