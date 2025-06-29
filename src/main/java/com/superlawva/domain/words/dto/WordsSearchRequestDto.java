package com.superlawva.domain.words.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "법률 용어 검색 요청 DTO")
public class WordsSearchRequestDto {

    @Schema(description = "검색할 법률 용어 키워드. 일부만 일치해도 검색됩니다.", example = "보증금")
    @Builder.Default
    private String keyword = "";

    @Schema(description = "결과 목록의 페이지 번호. 1부터 시작합니다.", example = "1", defaultValue = "1")
    @Builder.Default
    private int page = 1;

    @Schema(description = "한 페이지에 표시할 결과의 수. -1을 입력하면 전체 결과를 반환합니다.", example = "10", defaultValue = "10")
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