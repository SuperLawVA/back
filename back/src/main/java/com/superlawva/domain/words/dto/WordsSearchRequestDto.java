// WordsSearchRequestDto.java
package com.superlawva.domain.words.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WordsSearchRequestDto {
    
    private String keyword = "";
    
    @Builder.Default
    private int page = 1;
    
    @Builder.Default
    private int pageSize = 10;
    
    // 유효성 검사 메서드
    public void validateAndAdjust() {
        if (page < 1) {
            page = 1;
        }
        
        // pageSize = -1이면 전체 조회 (무제한)
        if (pageSize == -1) {
            pageSize = Integer.MAX_VALUE; // 전체 조회
        } else if (pageSize < 1 || pageSize > 1000) { // 최대 1000개로 증가
            pageSize = 50; // 기본값도 50개로 증가
        }
        
        if (keyword == null) {
            keyword = "";
        }
    }
}