package com.superlawva.domain.words.dto;

import com.superlawva.domain.words.entity.Words;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "법률 용어 DTO")
public class WordsDto {

    @Schema(description = "용어명", example = "보증금")
    private String word;    // 용어명 (Primary Key)
    
    @Schema(description = "용어 설명", example = "임대차계약에서 임차인이 임대인에게 제공하는 금전적 담보")
    private String content; // 용어 설명

    // Entity to DTO 변환
    public static WordsDto fromEntity(Words words) {
        return WordsDto.builder()
                .word(words.getWord())
                .content(words.getContent())
                .build();
    }
}