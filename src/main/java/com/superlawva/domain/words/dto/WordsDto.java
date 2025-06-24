// WordsDto.java
package com.superlawva.domain.words.dto;

import com.superlawva.domain.words.entity.Words;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WordsDto {
    
    private String word;    // 용어명 (Primary Key)
    private String content; // 용어 설명
    
    // Entity to DTO 변환
    public static WordsDto fromEntity(Words words) {
        return WordsDto.builder()
                .word(words.getWord())
                .content(words.getContent())
                .build();
    }
}
