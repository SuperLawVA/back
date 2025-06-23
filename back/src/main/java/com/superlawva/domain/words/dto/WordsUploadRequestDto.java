// WordsUploadRequestDto.java
package com.superlawva.domain.words.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WordsUploadRequestDto {
    
    @NotBlank(message = "용어명은 필수입니다.")
    @Size(max = 255, message = "용어명은 255자를 초과할 수 없습니다.")
    private String word;    // 용어명
    
    @NotBlank(message = "설명은 필수입니다.")
    private String content; // 용어 설명
}